-- ================================================================
-- 图书管理系统 — 测试脚本
-- 数据库: library_db | MySQL 8.0+
-- ================================================================

USE library_db;

-- ================================================================
-- 1. 数据一致性测试 — 库存校验
--    available_copies 应该 = total_copies - 当前借出数
--    例外：被 FULFILLED 预约锁定的书，库存不归公共
--    期望：差异列全部为 0
-- ================================================================
SELECT '=== 1. 库存一致性测试 ===' AS '';
SELECT
    b.id,
    b.title,
    b.total_copies       AS 总册数,
    b.available_copies   AS 可借数,
    COALESCE(br_cnt.borrowed, 0) AS 借出中,
    b.total_copies - COALESCE(br_cnt.borrowed, 0) - b.available_copies AS 差异
FROM books b
LEFT JOIN (
    SELECT book_id, COUNT(*) AS borrowed
    FROM borrow_records WHERE status = 'BORROWING'
    GROUP BY book_id
) br_cnt ON b.id = br_cnt.book_id
HAVING 差异 != 0;

-- ================================================================
-- 2. 存储过程测试
-- ================================================================
SELECT '=== 2. 存储过程测试 ===' AS '';

-- 2a. 正常借书（读者3 李四 借 机器学习 book_id=7，目前 available=4）
CALL sp_borrow_book(3, 7, @res, @msg);
SELECT @res AS 结果码, @msg AS 消息;

-- 2b. 重复借同一本（应失败，结果码=5）
CALL sp_borrow_book(3, 7, @res, @msg);
SELECT @res AS 结果码, @msg AS 消息;

-- 2c. 续借测试（李四之前借的 Spring实战 id=3 已归还，用张三的 Java核心技术 id=1）
CALL sp_renew_book(2, 1, @res, @msg);
SELECT @res AS 结果码, @msg AS 消息;

-- 2d. 过期预约处理
CALL sp_expire_reservations(@cnt);
SELECT @cnt AS 过期标记数;

-- ================================================================
-- 3. 触发器验证
-- ================================================================
SELECT '=== 3. 触发器测试 ===' AS '';

-- 3a. 试图借一本库存为 0 的书（深入理解Java虚拟机 book_id=2，available=0）
--    应被 trg_borrow_before_insert 拦截
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status)
VALUES (2, 2, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'BORROWING');

-- 3b. 未借过该书的用户试图评论
--    读者5(孙七) 未借过 人间失格(book_id=10)
INSERT INTO book_reviews (user_id, book_id, rating, content)
VALUES (6, 10, 5, '未借过但想评论');

-- ================================================================
-- 4. 视图查询验证
-- ================================================================
SELECT '=== 4. 视图查询 ===' AS '';
SELECT * FROM v_overdue_books;
SELECT * FROM v_book_rating WHERE review_count > 0;
SELECT * FROM v_reservation_queue;

-- ================================================================
-- 5. 函数调用验证
-- ================================================================
SELECT '=== 5. 函数测试 ===' AS '';

-- 逾期 15 天，书价 69 元 → 15×0.5=7.50（未超上限）
SELECT fn_calc_fine('2026-04-01', '2026-04-16', 69.00) AS 应罚金额;

-- 逾期 200 天，书价 38 元 → 封顶 38 元
SELECT fn_calc_fine('2026-01-01', '2026-05-29', 38.00) AS 应罚金额_封顶;

-- 王五(4) 有逾期 → 返回原因
SELECT fn_can_borrow(4) AS 王五状态;

-- 张三(2) 正常 → 返回空
SELECT fn_can_borrow(2) AS 张三状态;
