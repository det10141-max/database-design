-- ================================================================
-- 图书管理系统 — 查询与统计脚本
-- 数据库: library_db | MySQL 8.0+
-- 用途: 演示复杂查询、连接、聚合、子查询
-- ================================================================

USE library_db;

-- ================================================================
-- 1. 读者借阅排行榜（借阅次数 + 累计罚款）
-- ================================================================
SELECT u.real_name  AS 读者,
       COUNT(br.id) AS 借阅次数,
       COALESCE(SUM(f.amount), 0) AS 累计罚款
FROM users u
LEFT JOIN borrow_records br ON u.id = br.user_id
LEFT JOIN fines f ON u.id = f.user_id
WHERE u.role = 'READER'
GROUP BY u.id, u.real_name
ORDER BY 借阅次数 DESC
LIMIT 10;

-- ================================================================
-- 2. 热门图书 TOP 10（被借次数排行）
-- ================================================================
SELECT b.title  AS 书名,
       b.author AS 作者,
       COUNT(br.id) AS 被借次数
FROM books b
LEFT JOIN borrow_records br ON b.id = br.book_id
GROUP BY b.id, b.title, b.author
ORDER BY 被借次数 DESC
LIMIT 10;

-- ================================================================
-- 3. 当前逾期总览（统计汇总）
-- ================================================================
SELECT COUNT(*)  AS 逾期人数,
       ROUND(AVG(DATEDIFF(NOW(), due_date)), 1) AS 平均逾期天数,
       ROUND(SUM(LEAST(DATEDIFF(NOW(), due_date) * 0.50, b.price)), 2) AS 预计罚款总额
FROM borrow_records br
JOIN books b ON br.book_id = b.id
WHERE br.status = 'BORROWING' AND br.due_date < NOW();

-- ================================================================
-- 4. 近 6 个月月度借阅趋势
-- ================================================================
SELECT DATE_FORMAT(borrow_date, '%Y-%m') AS 月份,
       COUNT(*)       AS 借阅量,
       COUNT(DISTINCT user_id) AS 活跃用户数
FROM borrow_records
WHERE borrow_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
GROUP BY 月份
ORDER BY 月份;

-- ================================================================
-- 5. 各分类借阅分布
-- ================================================================
SELECT COALESCE(c.name, '未分类') AS 分类,
       COUNT(br.id) AS 借阅次数
FROM borrow_records br
JOIN books b ON br.book_id = b.id
LEFT JOIN categories c ON b.category_id = c.id
GROUP BY c.id, c.name
ORDER BY 借阅次数 DESC;

-- ================================================================
-- 6. 子查询演示 — "借过《活着》的人也借了什么"
-- ================================================================
SELECT DISTINCT b.title AS 书名,
       COUNT(*) AS 借阅人数
FROM borrow_records br
JOIN books b ON br.book_id = b.id
WHERE br.user_id IN (
    SELECT user_id FROM borrow_records WHERE book_id = 8
) AND br.book_id != 8
GROUP BY b.id, b.title
ORDER BY 借阅人数 DESC;
