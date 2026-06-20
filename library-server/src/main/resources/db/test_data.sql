-- ================================================================
-- 测试数据插入脚本（全部使用 admin123 作为密码）
-- 生成日期: 2026-05-29
-- ================================================================

USE library_db;

-- ================================================================
-- 1. 新增测试用户（密码 = admin123 的 BCrypt 哈希）
-- ================================================================
INSERT INTO users (username, password, real_name, role, phone, email, status) VALUES
('reader2', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '李四',   'READER', '13800001111', 'lisi@example.com',   1),
('reader3', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '王五',   'READER', '13800002222', 'wangwu@example.com', 1),
('reader4', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '赵六',   'READER', '13800003333', 'zhaoliu@example.com', 1),
('reader5', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '孙七',   'READER', '13800004444', 'sunqi@example.com',   1),
('reader6', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '周八',   'READER', '13800005555', 'zhouba@example.com',  0);

-- ================================================================
-- 2. 图书数据
-- ================================================================
INSERT INTO books (isbn, title, author, publisher, category_id, publish_year, edition, price, total_copies, available_copies, location, description, status) VALUES

-- 计算机科学 / 编程语言 (category 6)
('978-7-111-72653-9', 'Java核心技术 卷I',       'Cay S. Horstmann', '机械工业出版社', 6, 2022, '第12版', 149.00, 5, 3, 'A区-3排-5层', '全面讲解Java语言核心概念与API，是Java开发者必读经典。', 1),
('978-7-115-54473-9', '深入理解Java虚拟机',     '周志明',           '人民邮电出版社', 6, 2019, '第3版',  129.00, 3, 0, 'A区-3排-6层', 'JVM调优圣经，涵盖内存管理、类加载、编译优化等核心主题。', 1),
('978-7-121-42493-7', 'Spring实战 第6版',       'Craig Walls',     '电子工业出版社', 6, 2022, '第6版',  128.00, 3, 2, 'A区-3排-7层', 'Spring Framework与Spring Boot实战指南。', 1),

-- 数据库 (category 7)
('978-7-111-68531-5', '高性能MySQL 第4版',      'Silvia Botros',    '机械工业出版社', 7, 2021, '第4版',  139.00, 2, 1, 'A区-4排-1层', 'MySQL性能优化权威指南，涵盖索引、查询优化、复制与高可用。', 1),
('978-7-121-38816-2', 'MySQL必知必会',          'Ben Forta',       '电子工业出版社', 7, 2020, '第1版',   49.00, 5, 5, 'A区-4排-2层', 'MySQL入门经典，短小精悍，快速上手。', 1),

-- 人工智能 (category 8)
('978-7-111-64388-1', '深度学习',               'Ian Goodfellow',   '机械工业出版社', 8, 2020, '第1版',  168.00, 2, 1, 'A区-5排-3层', '深度学习领域奠基之作，被誉为"AI圣经"。', 1),
('978-7-302-58250-0', '机器学习',               '周志华',           '清华大学出版社', 8, 2021, '第2版',   88.00, 4, 4, 'A区-5排-4层', '机器学习入门经典，俗称"西瓜书"。', 1),

-- 文学小说 (category 2)
('978-7-02-018673-1', '活着',                   '余华',             '人民文学出版社', 2, 2012, '第3版',   38.00, 4, 2, 'B区-1排-1层', '讲述人在极端环境下生存的故事，震撼人心的文学杰作。', 1),
('978-7-5442-8019-8', '百年孤独',               '加西亚·马尔克斯',  '南海出版公司',   2, 2017, '第1版',   55.00, 2, 0, 'B区-1排-2层', '魔幻现实主义代表作，一个家族七代人的传奇故事。', 1),
('978-7-5302-1977-4', '人间失格',               '太宰治',           '北京十月文艺',   2, 2019, '第1版',   32.00, 2, 2, 'B区-1排-3层', '太宰治半自传体小说，日本文学经典。', 1),

-- 历史地理 (category 3)
('978-7-5080-9859-5', '万历十五年',             '黄仁宇',           '中华书局',       3, 2018, '第2版',   42.00, 3, 3, 'C区-2排-4层', '以万历十五年为切入点，剖析明朝政治制度的深层危机。', 1),
('978-7-5531-0896-5', '全球通史',               '斯塔夫里阿诺斯',   '北京大学出版社', 3, 2020, '第4版',   98.00, 2, 1, 'C区-2排-5层', '从全球视角讲述人类文明发展历程。', 1),

-- 自然科学 (category 4)
('978-7-5710-0918-7', '时间简史',               '史蒂芬·霍金',     '湖南科技出版社', 4, 2019, '第3版',   45.00, 3, 2, 'D区-1排-1层', '霍金关于宇宙本质的科普经典，通俗易懂。', 1),

-- 社会科学 (category 5)
('978-7-5086-6397-5', '思考，快与慢',           '丹尼尔·卡尼曼',   '中信出版社',     5, 2018, '第1版',   69.00, 3, 3, 'E区-3排-2层', '诺贝尔经济学奖得主的行为经济学经典著作。', 1),
('978-7-100-19614-8', '乡土中国',               '费孝通',           '商务印书馆',     5, 2021, '第4版',   36.00, 3, 3, 'E区-3排-4层', '了解中国基层社会结构的经典社会学著作。', 1);

-- ================================================================
-- 3. 借阅记录
-- 场景：
--   - 张三(2) 借了2本书：Java核心技术(正常)、活着(正常)
--   - 李四(3) 借了1本：Spring实战(已归还)
--   - 王五(4) 借了3本：深度学习(正常)、百年孤独(正常)、全球通史(逾期！)
--   - 孙七(6) 借了1本：MySQL必知必会(已归还)
-- ================================================================

-- 张三 — Java核心技术（正常借阅中，借了12天）
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, renew_count, status, created_at) VALUES
(2, 1, '2026-05-17 10:30:00', '2026-06-01 10:30:00', 0, 'BORROWING', '2026-05-17 10:30:00');

-- 张三 — 活着（正常借阅中，借了20天）
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, renew_count, status, created_at) VALUES
(2, 8, '2026-05-09 14:00:00', '2026-06-03 14:00:00', 0, 'BORROWING', '2026-05-09 14:00:00');

-- 李四 — Spring实战（已归还，借了25天，提前还）
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, return_date, renew_count, status, created_at) VALUES
(3, 3, '2026-04-15 09:00:00', '2026-05-15 09:00:00', '2026-05-10 16:30:00', 0, 'RETURNED', '2026-04-15 09:00:00');

-- 王五 — 深度学习（正常借阅中，借了10天）
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, renew_count, status, created_at) VALUES
(4, 6, '2026-05-19 11:00:00', '2026-06-10 11:00:00', 0, 'BORROWING', '2026-05-19 11:00:00');

-- 王五 — 百年孤独（正常借阅中，借了15天）
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, renew_count, status, created_at) VALUES
(4, 9, '2026-05-14 15:00:00', '2026-06-06 15:00:00', 0, 'BORROWING', '2026-05-14 15:00:00');

-- 王五 — 全球通史（逾期中！应还2026-04-28，续借过1次）
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, renew_count, status, created_at) VALUES
(4, 12, '2026-03-14 08:00:00', '2026-04-28 08:00:00', 1, 'BORROWING', '2026-03-14 08:00:00');

-- 孙七 — MySQL必知必会（已归还，借了14天）
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, return_date, renew_count, status, created_at) VALUES
(6, 5, '2026-05-01 13:00:00', '2026-05-31 13:00:00', '2026-05-15 10:00:00', 0, 'RETURNED', '2026-05-01 13:00:00');

-- 张三 — 时间简史（刚刚借出，借了2天）
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, renew_count, status, created_at) VALUES
(2, 13, '2026-05-27 16:45:00', '2026-06-18 16:45:00', 0, 'BORROWING', '2026-05-27 16:45:00');

-- ================================================================
-- 4. 预约记录
-- 场景：
--   - 深入理解Java虚拟机(book_id=2, available=0) → 3人排队
--   - 百年孤独(book_id=9, available=0) → 2人排队
--   - 高性能MySQL(book_id=4, available=1) → 1人预约（虽然还有1本可借，但可能在运输中）
-- ================================================================

-- 深入理解Java虚拟机 — 排队第1位（李四，预约中，即将到期）
INSERT INTO reservations (user_id, book_id, reserve_date, queue_position, expire_date, pickup_deadline, status, created_at) VALUES
(3, 2, '2026-05-22 09:00:00', 1, '2026-05-29 09:00:00', NULL, 'WAITING', '2026-05-22 09:00:00');

-- 深入理解Java虚拟机 — 排队第2位（王五）
INSERT INTO reservations (user_id, book_id, reserve_date, queue_position, expire_date, pickup_deadline, status, created_at) VALUES
(4, 2, '2026-05-25 11:30:00', 2, '2026-06-01 11:30:00', NULL, 'WAITING', '2026-05-25 11:30:00');

-- 深入理解Java虚拟机 — 排队第3位（孙七）
INSERT INTO reservations (user_id, book_id, reserve_date, queue_position, expire_date, pickup_deadline, status, created_at) VALUES
(6, 2, '2026-05-28 08:00:00', 3, '2026-06-04 08:00:00', NULL, 'WAITING', '2026-05-28 08:00:00');

-- 百年孤独 — 排队第1位（张三，已到书，等待取书）
INSERT INTO reservations (user_id, book_id, reserve_date, queue_position, expire_date, pickup_deadline, status, created_at) VALUES
(2, 9, '2026-05-10 10:00:00', 1, '2026-05-17 10:00:00', '2026-06-01 10:00:00', 'FULFILLED', '2026-05-10 10:00:00');

-- 百年孤独 — 排队第2位（李四，预约中）
INSERT INTO reservations (user_id, book_id, reserve_date, queue_position, expire_date, pickup_deadline, status, created_at) VALUES
(3, 9, '2026-05-18 14:00:00', 2, '2026-05-25 14:00:00', NULL, 'WAITING', '2026-05-18 14:00:00');

-- 高性能MySQL — 排队第1位（赵六，用户被禁用，此预约已被取消）
INSERT INTO reservations (user_id, book_id, reserve_date, queue_position, expire_date, pickup_deadline, status, created_at) VALUES
(5, 4, '2026-05-20 16:00:00', 1, '2026-05-27 16:00:00', NULL, 'CANCELLED', '2026-05-20 16:00:00');

-- 时间简史 — 孙七之前预约过，已过期
INSERT INTO reservations (user_id, book_id, reserve_date, queue_position, expire_date, pickup_deadline, status, created_at) VALUES
(6, 13, '2026-05-01 09:00:00', 1, '2026-05-08 09:00:00', NULL, 'EXPIRED', '2026-05-01 09:00:00');

-- ================================================================
-- 5. 罚款记录
-- 场景：
--   - 王五(4) 全球通史逾期（2026-04-28应还，至今逾期31天，0.5元/天 = 15.50元）
--   - 张三(2) 之前借过一本书丢失已赔付
-- ================================================================

-- 王五 — 全球通史逾期罚款（未缴）
INSERT INTO fines (user_id, borrow_record_id, amount, reason, status, created_at) VALUES
(4, 6, 15.50, '逾期-全球通史(应还2026-04-28，已逾期31天)', 'UNPAID', NOW());

-- 张三 — 历史丢失赔付（已缴）
INSERT INTO fines (user_id, borrow_record_id, amount, reason, status, paid_at, created_at) VALUES
(2, NULL, 69.00, '丢失-思考快与慢(原价69元赔偿)', 'PAID', '2026-05-20 10:00:00', '2026-05-18 09:00:00');

-- ================================================================
-- 6. 书评
-- ================================================================

-- 张三评「活着」
INSERT INTO book_reviews (user_id, book_id, rating, content, created_at) VALUES
(2, 8, 5, '读完泪流满面。余华用最简单的文字写出了最沉重的故事，每个人物都让人难忘。强烈推荐！', '2026-05-20 20:30:00');

-- 李四评「Spring实战」
INSERT INTO book_reviews (user_id, book_id, rating, content, created_at) VALUES
(3, 3, 4, '内容全面，代码示例清晰，适合有一定Java基础的读者。唯独部分章节组织略显松散。', '2026-05-12 15:00:00');

-- 王五评「深度学习」★★★★★
INSERT INTO book_reviews (user_id, book_id, rating, content, created_at) VALUES
(4, 6, 5, '花书名不虚传，数学推导严谨，需要一定的基础。建议先看吴恩达课程再啃这本。', '2026-05-22 09:30:00');

-- 孙七评「MySQL必知必会」
INSERT INTO book_reviews (user_id, book_id, rating, content, created_at) VALUES
(6, 5, 4, '非常适合入门，两天就能看完，可以快速上手SQL。但深度不够，看完需要进阶。', '2026-05-16 18:00:00');

-- 张三评「时间简史」
INSERT INTO book_reviews (user_id, book_id, rating, content, created_at) VALUES
(2, 13, 4, '霍金把深奥的宇宙学讲得通俗有趣，但读到后面还是有些吃力。值得反复阅读。', '2026-05-28 21:00:00');

-- 李四评「百年孤独」
INSERT INTO book_reviews (user_id, book_id, rating, content, created_at) VALUES
(3, 9, 5, '人名确实难记（建议边看边画族谱），但一旦沉浸进去，那种魔幻的命运感令人震撼。', '2026-05-25 22:00:00');

-- ================================================================
-- 7. 公告
-- ================================================================

INSERT INTO announcements (title, content, publisher_id, is_pinned, status, created_at) VALUES
('五一假期开馆时间调整通知', '各位读者：五一劳动节期间（5月1日-5月5日），图书馆开放时间调整为9:00-17:00。请合理安排借还书时间。', 1, 1, 1, '2026-04-28 09:00:00'),
('关于逾期罚款新标准的公告', '根据学校最新规定，自2026年5月起，图书逾期罚款标准调整为每日0.50元。请各位读者按时归还图书，避免产生罚款。', 1, 1, 1, '2026-05-01 08:30:00'),
('新增电子资源数据库试用通知', '图书馆已开通IEEE Xplore数据库三个月试用期（2026.5.15-8.15），欢迎广大师生使用。访问方式详见图书馆主页。', 1, 0, 1, '2026-05-15 10:00:00'),
('"书香校园"读书月活动启动', '6月将举办"书香校园"主题读书月活动，包括好书推荐、读书分享会、书评大赛等。欢迎踊跃报名参加！', 1, 0, 1, '2026-05-26 14:00:00');

-- ================================================================
-- 8. 通知
-- ================================================================

-- 王五 — 逾期提醒
INSERT INTO notifications (user_id, type, title, content, is_read, ref_id, created_at) VALUES
(4, 'OVERDUE', '图书逾期提醒', '您借阅的《全球通史》已逾期31天，请尽快归还并缴纳罚款15.50元。', 0, 6, '2026-05-28 08:00:00');

-- 张三 — 预约到书通知
INSERT INTO notifications (user_id, type, title, content, is_read, ref_id, created_at) VALUES
(2, 'RESERVE_READY', '预约到书通知', '您预约的《百年孤独》已到馆，请在2026-06-01前到B区服务台办理借阅。', 1, 4, '2026-05-28 09:00:00');

-- 李四 — 预约即将到期提醒
INSERT INTO notifications (user_id, type, title, content, is_read, ref_id, created_at) VALUES
(3, 'RESERVE_READY', '预约即将到期', '您预约的《深入理解Java虚拟机》即将到期，若书籍到馆将按排队顺序通知。', 0, 1, '2026-05-28 10:00:00');

-- 张三 — 罚款已缴确认
INSERT INTO notifications (user_id, type, title, content, is_read, ref_id, created_at) VALUES
(2, 'FINE', '罚款缴纳确认', '您于2026-05-20缴纳的图书丢失赔偿69.00元已确认，感谢配合。', 1, 2, '2026-05-20 10:05:00');

-- 全员公告通知（给张三）
INSERT INTO notifications (user_id, type, title, content, is_read, ref_id, created_at) VALUES
(2, 'ANNOUNCE', '"书香校园"读书月活动启动', '6月将举办"书香校园"主题读书月活动，欢迎参加。', 0, 4, '2026-05-26 14:05:00');

-- 全员公告通知（给李四）
INSERT INTO notifications (user_id, type, title, content, is_read, ref_id, created_at) VALUES
(3, 'ANNOUNCE', '"书香校园"读书月活动启动', '6月将举办"书香校园"主题读书月活动，欢迎参加。', 0, 4, '2026-05-26 14:05:00');

-- 全员公告通知（给王五）
INSERT INTO notifications (user_id, type, title, content, is_read, ref_id, created_at) VALUES
(4, 'ANNOUNCE', '"书香校园"读书月活动启动', '6月将举办"书香校园"主题读书月活动，欢迎参加。', 0, 4, '2026-05-26 14:05:00');

-- ================================================================
-- 9. 操作日志（管理员操作记录）
-- ================================================================

INSERT INTO operation_logs (user_id, action, target_type, target_id, detail, ip, created_at) VALUES
(1, 'ADD_BOOK', 'Book', 1, '新增图书《Java核心技术 卷I》', '127.0.0.1', '2026-05-01 09:30:00'),
(1, 'ADD_BOOK', 'Book', 2, '新增图书《深入理解Java虚拟机》', '127.0.0.1', '2026-05-01 09:35:00'),
(1, 'ADD_BOOK', 'Book', 8, '新增图书《活着》', '127.0.0.1', '2026-05-01 10:00:00'),
(1, 'PUBLISH_ANNOUNCE', 'Announcement', 1, '发布公告"五一假期开馆时间调整通知"', '127.0.0.1', '2026-04-28 09:00:00'),
(1, 'PUBLISH_ANNOUNCE', 'Announcement', 2, '发布公告"关于逾期罚款新标准的公告"', '127.0.0.1', '2026-05-01 08:30:00'),
(1, 'RESOLVE_FINE', 'Fine', 2, '处理罚款-张三图书丢失赔偿69.00元已缴纳', '127.0.0.1', '2026-05-20 10:00:00'),
(1, 'DISABLE_USER', 'User', 5, '禁用用户赵六(reader6)-多次逾期不还', '127.0.0.1', '2026-05-15 16:00:00');
