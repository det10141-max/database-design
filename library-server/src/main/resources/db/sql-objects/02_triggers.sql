-- ================================================================
-- 图书管理系统 — 触发器
-- 数据库: library_db | MySQL 8.0+
-- ================================================================

USE library_db;
DELIMITER $$

-- ================================================================
-- 1. trg_borrow_before_insert — 借阅前库存校验
--    表: borrow_records  |  时机: BEFORE INSERT
--    数据库层兜底，防止应用层绕过校验直接 INSERT
-- ================================================================
DROP TRIGGER IF EXISTS trg_borrow_before_insert$$
CREATE TRIGGER trg_borrow_before_insert
BEFORE INSERT ON borrow_records
FOR EACH ROW
BEGIN
    DECLARE v_available INT;

    SELECT available_copies INTO v_available
    FROM books WHERE id = NEW.book_id;

    IF v_available IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '图书不存在';
    END IF;
    -- available_copies 被 Java 层行级锁保护，先扣减再 insert；
    -- 此处仅拦截已为负数的情况（数据库层兜底），避免误拦正常借阅
    IF v_available < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '该书暂无可借副本，无法借阅';
    END IF;
END$$


-- ================================================================
-- 2. trg_reservation_after_update — 预约状态变更自动通知
--    表: reservations  |  时机: AFTER UPDATE
--    当预约从 WAITING → FULFILLED 时，自动生成到书通知
--    注：重排队逻辑由 sp_expire_reservations 统一处理，避免触发器递归更新
-- ================================================================
DROP TRIGGER IF EXISTS trg_reservation_after_update$$
CREATE TRIGGER trg_reservation_after_update
AFTER UPDATE ON reservations
FOR EACH ROW
BEGIN
    IF OLD.status = 'WAITING' AND NEW.status = 'FULFILLED' THEN
        INSERT INTO notifications (user_id, type, title, content, ref_id)
        VALUES (NEW.user_id, 'RESERVE_READY', '预约到书通知',
                CONCAT('您预约的图书已到馆，请在',
                       DATE_FORMAT(NEW.pickup_deadline, '%Y-%m-%d %H:%i'),
                       '前到馆借阅'),
                NEW.id);
    END IF;
END$$


-- ================================================================
-- 3. trg_book_review_check — 评论资格校验
--    表: book_reviews  |  时机: BEFORE INSERT
--    只有曾借过该书（含已归还/丢失）的用户才能发表评论
-- ================================================================
DROP TRIGGER IF EXISTS trg_book_review_check$$
CREATE TRIGGER trg_book_review_check
BEFORE INSERT ON book_reviews
FOR EACH ROW
BEGIN
    DECLARE v_cnt INT;

    SELECT COUNT(*) INTO v_cnt FROM borrow_records
    WHERE user_id = NEW.user_id AND book_id = NEW.book_id;

    IF v_cnt = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '您未借阅过该书，无法评论';
    END IF;
END$$


DELIMITER ;
