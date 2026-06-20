-- ================================================================
-- 图书管理系统 — 视图
-- 数据库: library_db | MySQL 8.0+
-- ================================================================

USE library_db;

-- ================================================================
-- 1. v_borrow_detail — 借阅详情视图（含书名、读者名、剩余天数）
-- ================================================================
DROP VIEW IF EXISTS v_borrow_detail;
CREATE VIEW v_borrow_detail AS
SELECT
    br.id,
    br.user_id,
    u.real_name   AS user_name,
    br.book_id,
    b.title       AS book_title,
    b.author,
    b.isbn,
    br.borrow_date,
    br.due_date,
    br.return_date,
    br.renew_count,
    br.status,
    DATEDIFF(br.due_date, NOW()) AS days_remaining
FROM borrow_records br
JOIN users u ON br.user_id = u.id
JOIN books b ON br.book_id = b.id;


-- ================================================================
-- 2. v_overdue_books — 逾期图书一览
-- ================================================================
DROP VIEW IF EXISTS v_overdue_books;
CREATE VIEW v_overdue_books AS
SELECT
    br.id,
    u.real_name   AS user_name,
    u.phone,
    b.title       AS book_title,
    b.author,
    br.borrow_date,
    br.due_date,
    DATEDIFF(NOW(), br.due_date)           AS overdue_days,
    LEAST(DATEDIFF(NOW(), br.due_date) * 0.50, b.price) AS estimated_fine
FROM borrow_records br
JOIN users u ON br.user_id = u.id
JOIN books b ON br.book_id = b.id
WHERE br.status = 'BORROWING' AND br.due_date < NOW()
ORDER BY overdue_days DESC;


-- ================================================================
-- 3. v_reservation_queue — 预约排队视图
-- ================================================================
DROP VIEW IF EXISTS v_reservation_queue;
CREATE VIEW v_reservation_queue AS
SELECT
    r.id,
    r.book_id,
    b.title       AS book_title,
    b.author,
    r.user_id,
    u.real_name   AS user_name,
    r.queue_position,
    r.reserve_date,
    r.expire_date,
    r.pickup_deadline,
    r.status
FROM reservations r
JOIN books b ON r.book_id = b.id
JOIN users u ON r.user_id = u.id
ORDER BY r.book_id, r.queue_position;


-- ================================================================
-- 4. v_book_rating — 图书评分汇总视图
-- ================================================================
DROP VIEW IF EXISTS v_book_rating;
CREATE VIEW v_book_rating AS
SELECT
    b.id          AS book_id,
    b.title,
    b.author,
    b.publisher,
    COUNT(rv.id)  AS review_count,
    ROUND(IFNULL(AVG(rv.rating), 0), 1) AS avg_rating,
    COUNT(CASE WHEN rv.rating = 5 THEN 1 END) AS five_star,
    COUNT(CASE WHEN rv.rating = 4 THEN 1 END) AS four_star,
    COUNT(CASE WHEN rv.rating <= 3 THEN 1 END) AS low_star
FROM books b
LEFT JOIN book_reviews rv ON b.id = rv.book_id
GROUP BY b.id, b.title, b.author, b.publisher;
