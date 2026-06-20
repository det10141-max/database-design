-- ================================================================
-- 图书管理系统 — 函数
-- 数据库: library_db | MySQL 8.0+
-- ================================================================

USE library_db;
DELIMITER $$

-- ================================================================
-- 1. fn_calc_fine — 计算逾期罚款金额
--    参数: p_due_date (应还日期), p_return_date (实际归还日期), p_book_price (书价)
--    返回: DECIMAL(10,2)
--    规则: 日费率 0.50 元/天 × 逾期天数，上限不超过书价
-- ================================================================
DROP FUNCTION IF EXISTS fn_calc_fine$$
CREATE FUNCTION fn_calc_fine(
    p_due_date    DATETIME,
    p_return_date DATETIME,
    p_book_price  DECIMAL(10,2)
) RETURNS DECIMAL(10,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_days INT;
    DECLARE v_fine DECIMAL(10,2);

    IF p_return_date IS NULL OR p_return_date <= p_due_date THEN
        RETURN 0;
    END IF;

    SET v_days = DATEDIFF(p_return_date, p_due_date);
    SET v_fine = LEAST(v_days * 0.50, p_book_price);
    RETURN v_fine;
END$$


-- ================================================================
-- 2. fn_can_borrow — 判断用户当前是否可借书
--    参数: p_user_id
--    返回: VARCHAR(200) — 空字符串表示可借，否则返回不能借的原因
-- ================================================================
DROP FUNCTION IF EXISTS fn_can_borrow$$
CREATE FUNCTION fn_can_borrow(p_user_id BIGINT)
RETURNS VARCHAR(200)
READS SQL DATA
BEGIN
    DECLARE v_status     INT;
    DECLARE v_max_borrow INT;
    DECLARE v_overdue    BIGINT;
    DECLARE v_current    BIGINT;
    DECLARE v_fine       BIGINT;

    -- 用户状态
    SELECT status, max_borrow INTO v_status, v_max_borrow
    FROM users WHERE id = p_user_id;
    IF v_status IS NULL THEN
        RETURN '用户不存在';
    END IF;
    IF v_status = 0 THEN
        RETURN '账号已被禁用';
    END IF;

    -- 逾期检查
    SELECT COUNT(*) INTO v_overdue FROM borrow_records
    WHERE user_id = p_user_id AND status = 'BORROWING' AND due_date < NOW();
    IF v_overdue > 0 THEN
        RETURN '存在逾期未还图书';
    END IF;

    -- 借阅上限
    SELECT COUNT(*) INTO v_current FROM borrow_records
    WHERE user_id = p_user_id AND status = 'BORROWING';
    IF v_current >= v_max_borrow THEN
        RETURN '已达最大借阅数';
    END IF;

    -- 未缴罚款
    SELECT COUNT(*) INTO v_fine FROM fines
    WHERE user_id = p_user_id AND status = 'UNPAID';
    IF v_fine > 0 THEN
        RETURN '存在未缴罚款';
    END IF;

    RETURN '';
END$$


DELIMITER ;
