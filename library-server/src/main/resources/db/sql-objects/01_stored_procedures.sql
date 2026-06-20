-- ================================================================
-- 图书管理系统 — 存储过程
-- 数据库: library_db | MySQL 8.0+
-- ================================================================

USE library_db;
DELIMITER $$

-- ================================================================
-- 1. sp_borrow_book — 借书原子操作
-- 返回: p_result (0=成功, 1=用户异常, 2=有逾期, 3=超上限, 4=有罚款, 5=重复借, 6=无库存)
-- ================================================================
DROP PROCEDURE IF EXISTS sp_borrow_book$$
CREATE PROCEDURE sp_borrow_book(
    IN  p_user_id  BIGINT,
    IN  p_book_id  BIGINT,
    OUT p_result   INT,
    OUT p_msg      VARCHAR(200)
)
sp_label: BEGIN
    DECLARE v_status      INT;
    DECLARE v_max_borrow  INT;
    DECLARE v_overdue     BIGINT;
    DECLARE v_current     BIGINT;
    DECLARE v_fine        BIGINT;
    DECLARE v_dup         BIGINT;
    DECLARE v_available   INT;
    DECLARE v_book_price  DECIMAL(10,2);
    DECLARE v_duration    INT DEFAULT 30;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 99;
        SET p_msg = '系统异常，借阅失败';
    END;

    START TRANSACTION;

    -- ① 校验用户
    SELECT status, max_borrow INTO v_status, v_max_borrow
    FROM users WHERE id = p_user_id;
    IF v_status IS NULL OR v_status = 0 THEN
        ROLLBACK;
        SET p_result = 1; SET p_msg = '用户状态异常';
        LEAVE sp_label;
    END IF;

    -- ② 校验逾期
    SELECT COUNT(*) INTO v_overdue FROM borrow_records
    WHERE user_id = p_user_id AND status = 'BORROWING' AND due_date < NOW();
    IF v_overdue > 0 THEN
        ROLLBACK;
        SET p_result = 2; SET p_msg = '存在逾期未还图书，请先归还';
        LEAVE sp_label;
    END IF;

    -- ③ 校验借阅上限
    SELECT COUNT(*) INTO v_current FROM borrow_records
    WHERE user_id = p_user_id AND status = 'BORROWING';
    IF v_current >= v_max_borrow THEN
        ROLLBACK;
        SET p_result = 3; SET p_msg = CONCAT('已达最大借阅数: ', v_max_borrow);
        LEAVE sp_label;
    END IF;

    -- ④ 校验未缴罚款
    SELECT COUNT(*) INTO v_fine FROM fines
    WHERE user_id = p_user_id AND status = 'UNPAID';
    IF v_fine > 0 THEN
        ROLLBACK;
        SET p_result = 4; SET p_msg = '存在未缴罚款，请先缴纳';
        LEAVE sp_label;
    END IF;

    -- ⑤ 校验重复借阅
    SELECT COUNT(*) INTO v_dup FROM borrow_records
    WHERE user_id = p_user_id AND book_id = p_book_id AND status = 'BORROWING';
    IF v_dup > 0 THEN
        ROLLBACK;
        SET p_result = 5; SET p_msg = '您已借阅该书，不可重复借阅';
        LEAVE sp_label;
    END IF;

    -- ⑥ 行级锁读库存
    SELECT available_copies, price INTO v_available, v_book_price
    FROM books WHERE id = p_book_id FOR UPDATE;
    IF v_available IS NULL THEN
        ROLLBACK;
        SET p_result = 6; SET p_msg = '图书不存在';
        LEAVE sp_label;
    END IF;
    IF v_available <= 0 THEN
        ROLLBACK;
        SET p_result = 6; SET p_msg = '该书暂无可借副本，请预约';
        LEAVE sp_label;
    END IF;

    -- ⑦ 扣库存
    UPDATE books SET available_copies = available_copies - 1 WHERE id = p_book_id;

    -- ⑧ 写入借阅记录
    INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status)
    VALUES (p_user_id, p_book_id, NOW(), DATE_ADD(NOW(), INTERVAL v_duration DAY), 'BORROWING');

    -- ⑨ 取消该用户对同一本书的预约（如果存在）
    UPDATE reservations SET status = 'CANCELLED'
    WHERE user_id = p_user_id AND book_id = p_book_id AND status = 'WAITING';

    COMMIT;
    SET p_result = 0; SET p_msg = '借阅成功';
END sp_label$$


-- ================================================================
-- 2. sp_return_book — 还书操作（含逾期罚款 + 预约队列处理）
-- ================================================================
DROP PROCEDURE IF EXISTS sp_return_book$$
CREATE PROCEDURE sp_return_book(
    IN  p_borrow_id   BIGINT,
    OUT p_result      INT,
    OUT p_msg         VARCHAR(200),
    OUT p_fine_amount DECIMAL(10,2)
)
sp_label: BEGIN
    DECLARE v_user_id     BIGINT;
    DECLARE v_book_id     BIGINT;
    DECLARE v_due_date    DATETIME;
    DECLARE v_return      DATETIME DEFAULT NOW();
    DECLARE v_status      VARCHAR(20);
    DECLARE v_days        INT;
    DECLARE v_book_price  DECIMAL(10,2);
    DECLARE v_fine        DECIMAL(10,2);
    DECLARE v_first_id    BIGINT;
    DECLARE v_reserve_uid BIGINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 99; SET p_msg = '系统异常，归还失败';
    END;

    START TRANSACTION;

    -- ① 锁借阅记录
    SELECT user_id, book_id, due_date, status
    INTO v_user_id, v_book_id, v_due_date, v_status
    FROM borrow_records WHERE id = p_borrow_id FOR UPDATE;

    IF v_user_id IS NULL THEN
        ROLLBACK;
        SET p_result = 1; SET p_msg = '借阅记录不存在';
        LEAVE sp_label;
    END IF;
    IF v_status != 'BORROWING' THEN
        ROLLBACK;
        SET p_result = 2; SET p_msg = '该记录非借阅中状态';
        LEAVE sp_label;
    END IF;

    -- ② 更新归还
    UPDATE borrow_records
    SET return_date = v_return, status = 'RETURNED'
    WHERE id = p_borrow_id;

    -- ③ 逾期罚款
    SET p_fine_amount = 0;
    IF v_return > v_due_date THEN
        SET v_days = DATEDIFF(v_return, v_due_date);
        SELECT price INTO v_book_price FROM books WHERE id = v_book_id;
        SET v_fine = LEAST(v_days * 0.50, v_book_price);
        SET p_fine_amount = v_fine;

        INSERT INTO fines (user_id, borrow_record_id, amount, reason, status)
        VALUES (v_user_id, p_borrow_id, v_fine, '逾期归还', 'UNPAID');

        INSERT INTO notifications (user_id, type, title, content, ref_id)
        VALUES (v_user_id, 'FINE', '逾期罚款通知',
                CONCAT('您归还的图书逾期', v_days, '天，产生罚款', v_fine, '元'),
                LAST_INSERT_ID());
    END IF;

    -- ④ 预约队列处理
    SELECT r.id, r.user_id INTO v_first_id, v_reserve_uid
    FROM reservations r
    WHERE r.book_id = v_book_id AND r.status = 'WAITING'
    ORDER BY r.queue_position ASC LIMIT 1;

    IF v_first_id IS NOT NULL THEN
        UPDATE reservations
        SET status = 'FULFILLED', pickup_deadline = DATE_ADD(NOW(), INTERVAL 3 DAY)
        WHERE id = v_first_id;

        INSERT INTO notifications (user_id, type, title, content, ref_id)
        VALUES (v_reserve_uid, 'RESERVE_READY', '预约到书通知',
                '您预约的图书已到馆，请在3天内到馆借阅', v_first_id);
    ELSE
        UPDATE books SET available_copies = available_copies + 1 WHERE id = v_book_id;
    END IF;

    COMMIT;
    SET p_result = 0; SET p_msg = '归还成功';
END sp_label$$


-- ================================================================
-- 3. sp_renew_book — 续借操作
-- ================================================================
DROP PROCEDURE IF EXISTS sp_renew_book$$
CREATE PROCEDURE sp_renew_book(
    IN  p_user_id   BIGINT,
    IN  p_borrow_id BIGINT,
    OUT p_result    INT,
    OUT p_msg       VARCHAR(200)
)
BEGIN
    DECLARE v_record_uid  BIGINT;
    DECLARE v_book_id     BIGINT;
    DECLARE v_due_date    DATETIME;
    DECLARE v_status      VARCHAR(20);
    DECLARE v_renew_cnt   INT;
    DECLARE v_renew_days  INT DEFAULT 30;
    DECLARE v_max_renew   INT DEFAULT 2;
    DECLARE v_reserve     BIGINT;

    -- 查询记录，同时校验归属
    SELECT user_id, book_id, due_date, status, renew_count
    INTO v_record_uid, v_book_id, v_due_date, v_status, v_renew_cnt
    FROM borrow_records WHERE id = p_borrow_id;

    IF v_book_id IS NULL OR v_record_uid != p_user_id THEN
        SET p_result = 1; SET p_msg = '借阅记录不存在';
    ELSEIF v_status != 'BORROWING' THEN
        SET p_result = 2; SET p_msg = '仅借阅中状态可续借';
    ELSEIF v_due_date < NOW() THEN
        SET p_result = 3; SET p_msg = '已逾期，不可续借';
    ELSEIF v_renew_cnt >= v_max_renew THEN
        SET p_result = 4; SET p_msg = '已达到最大续借次数';
    ELSE
        SELECT COUNT(*) INTO v_reserve FROM reservations
        WHERE book_id = v_book_id AND status = 'WAITING';
        IF v_reserve > 0 THEN
            SET p_result = 5; SET p_msg = '该书有他人预约，不可续借';
        ELSE
            UPDATE borrow_records
            SET due_date = DATE_ADD(due_date, INTERVAL v_renew_days DAY),
                renew_count = renew_count + 1
            WHERE id = p_borrow_id;
            SET p_result = 0; SET p_msg = '续借成功';
        END IF;
    END IF;
END$$


-- ================================================================
-- 4. sp_expire_reservations — 批量过期预约 + 重排队
-- ================================================================
DROP PROCEDURE IF EXISTS sp_expire_reservations$$
CREATE PROCEDURE sp_expire_reservations(OUT p_affected INT)
sp_label: BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_affected = -1;
    END;

    START TRANSACTION;

    UPDATE reservations SET status = 'EXPIRED'
    WHERE status = 'WAITING' AND expire_date < NOW();
    SET p_affected = ROW_COUNT();

    UPDATE reservations r
    JOIN (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY book_id ORDER BY created_at ASC) AS new_pos
        FROM reservations WHERE status = 'WAITING'
    ) sub ON r.id = sub.id
    SET r.queue_position = sub.new_pos;

    COMMIT;
END sp_label$$


DELIMITER ;
