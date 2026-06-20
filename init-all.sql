-- ================================================================
-- 图书管理系统 — 一键初始化脚本
-- 版本: v2.0 | 数据库: MySQL 8.0+ | 引擎: InnoDB
-- 字符集: utf8mb4 | 排序规则: utf8mb4_unicode_ci
--
-- 用法:
--   mysql -u root -p < init-all.sql
--
-- 功能:
--   ✅ 创建数据库 + 12 张表 + 外键 + 索引
--   ✅ 种子数据（2 用户 + 8 分类）
--   ✅ 测试数据（5 额外用户 + 15 图书 + 借阅/预约/罚款/评论/公告/通知/日志）
--   ✅ 4 个存储过程 + 3 个触发器 + 4 个视图 + 2 个函数
-- ================================================================

-- ================================================================
-- 阶段 1：数据库与表结构
-- ================================================================

CREATE DATABASE IF NOT EXISTS library_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE library_db;

-- ---------- 1. users ----------
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    username    VARCHAR(50)     NOT NULL                 COMMENT '登录名',
    password    VARCHAR(255)    NOT NULL                 COMMENT 'BCrypt 加密',
    real_name   VARCHAR(50)     NOT NULL                 COMMENT '真实姓名',
    role        ENUM('ADMIN','READER') NOT NULL          COMMENT '角色',
    phone       VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    email       VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    avatar_url  VARCHAR(255)    DEFAULT NULL             COMMENT '头像',
    status      TINYINT         DEFAULT 1                COMMENT '1=正常 0=禁用',
    max_borrow  INT             DEFAULT 5                COMMENT '最大可借数（读者）',
    is_deleted  TINYINT         DEFAULT 0                COMMENT '逻辑删除 1=已删',
    created_at  DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    updated_at  DATETIME        DEFAULT NOW() ON UPDATE NOW() COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ---------- 2. categories ----------
CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    name        VARCHAR(50)     NOT NULL                 COMMENT '分类名称',
    parent_id   BIGINT          DEFAULT 0                COMMENT '上级分类ID，0=顶级',
    sort_order  INT             DEFAULT 0                COMMENT '排序',
    created_at  DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- ---------- 3. books ----------
CREATE TABLE IF NOT EXISTS books (
    id               BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '图书ID',
    isbn             VARCHAR(20)     NOT NULL                 COMMENT 'ISBN',
    title            VARCHAR(200)    NOT NULL                 COMMENT '书名',
    author           VARCHAR(100)    NOT NULL                 COMMENT '作者',
    publisher        VARCHAR(100)    DEFAULT NULL             COMMENT '出版社',
    category_id      BIGINT          DEFAULT NULL             COMMENT '分类',
    publish_year     INT             DEFAULT NULL             COMMENT '出版年份',
    edition          VARCHAR(20)     DEFAULT ''               COMMENT '版次',
    price            DECIMAL(10,2)   DEFAULT NULL             COMMENT '定价',
    total_copies     INT             NOT NULL                 COMMENT '总册数',
    available_copies INT             NOT NULL                 COMMENT '当前可借册数',
    location         VARCHAR(50)     DEFAULT NULL             COMMENT '馆藏位置',
    cover_url        VARCHAR(255)    DEFAULT NULL             COMMENT '封面图片',
    description      TEXT            DEFAULT NULL             COMMENT '简介',
    status           TINYINT         DEFAULT 1                COMMENT '1=在架 0=下架',
    created_at       DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    updated_at       DATETIME        DEFAULT NOW() ON UPDATE NOW() COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_isbn (isbn),
    KEY idx_title (title),
    KEY idx_author (author),
    KEY idx_category_id (category_id),
    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- ---------- 4. borrow_records ----------
CREATE TABLE IF NOT EXISTS borrow_records (
    id           BIGINT                                NOT NULL AUTO_INCREMENT  COMMENT '借阅ID',
    user_id      BIGINT                                NOT NULL                 COMMENT '借阅人',
    book_id      BIGINT                                NOT NULL                 COMMENT '图书',
    borrow_date  DATETIME                              NOT NULL                 COMMENT '借出日期',
    due_date     DATETIME                              NOT NULL                 COMMENT '应还日期',
    return_date  DATETIME                              DEFAULT NULL             COMMENT '实际归还日期',
    renew_count  INT                                   DEFAULT 0                COMMENT '续借次数',
    status       ENUM('BORROWING','RETURNED','LOST')   NOT NULL                 COMMENT '状态',
    created_at   DATETIME                              DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_status (status),
    CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- ---------- 5. reservations ----------
CREATE TABLE IF NOT EXISTS reservations (
    id               BIGINT                                                NOT NULL AUTO_INCREMENT  COMMENT '预约ID',
    user_id          BIGINT                                                NOT NULL                 COMMENT '用户',
    book_id          BIGINT                                                NOT NULL                 COMMENT '图书',
    reserve_date     DATETIME                                              NOT NULL                 COMMENT '预约时间',
    queue_position   INT                                                   DEFAULT 0                COMMENT '排队序号',
    expire_date      DATETIME                                              DEFAULT NULL             COMMENT '预约失效时间',
    pickup_deadline  DATETIME                                              DEFAULT NULL             COMMENT '到书保留截止时间',
    status           ENUM('WAITING','FULFILLED','CANCELLED','EXPIRED')     NOT NULL                 COMMENT '状态',
    created_at       DATETIME                                              DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_status (status),
    CONSTRAINT fk_reserve_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reserve_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- ---------- 6. fines ----------
CREATE TABLE IF NOT EXISTS fines (
    id               BIGINT                        NOT NULL AUTO_INCREMENT  COMMENT '罚款ID',
    user_id          BIGINT                        NOT NULL                 COMMENT '用户',
    borrow_record_id BIGINT                        DEFAULT NULL             COMMENT '关联借阅记录',
    amount           DECIMAL(10,2)                 NOT NULL                 COMMENT '罚款金额',
    reason           VARCHAR(255)                  DEFAULT NULL             COMMENT '原因',
    status           ENUM('UNPAID','PAID')         DEFAULT 'UNPAID'         COMMENT '状态',
    paid_at          DATETIME                      DEFAULT NULL             COMMENT '缴纳时间',
    created_at       DATETIME                      DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_borrow_record_id (borrow_record_id),
    CONSTRAINT fk_fine_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_fine_borrow FOREIGN KEY (borrow_record_id) REFERENCES borrow_records(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='罚款表';

-- ---------- 7. book_reviews ----------
CREATE TABLE IF NOT EXISTS book_reviews (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '评论ID',
    user_id     BIGINT       NOT NULL                 COMMENT '用户',
    book_id     BIGINT       NOT NULL                 COMMENT '图书',
    rating      TINYINT      NOT NULL                 COMMENT '评分(1-5)',
    content     TEXT         DEFAULT NULL             COMMENT '评论内容',
    created_at  DATETIME     DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_book (user_id, book_id),
    KEY idx_book_id (book_id),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_review_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书评表';

-- ---------- 8. announcements ----------
CREATE TABLE IF NOT EXISTS announcements (
    id           BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '公告ID',
    title        VARCHAR(200)    NOT NULL                 COMMENT '标题',
    content      TEXT            DEFAULT NULL             COMMENT '内容',
    publisher_id BIGINT          NOT NULL                 COMMENT '发布人(管理员)',
    is_pinned    TINYINT         DEFAULT 0                COMMENT '置顶',
    status       TINYINT         DEFAULT 1                COMMENT '1=发布 0=草稿',
    publish_at   DATETIME        DEFAULT NULL             COMMENT '定时发布时间',
    created_at   DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    updated_at   DATETIME        DEFAULT NOW() ON UPDATE NOW() COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_publisher (publisher_id),
    KEY idx_status (status),
    CONSTRAINT fk_announce_publisher FOREIGN KEY (publisher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ---------- 9. notifications ----------
CREATE TABLE IF NOT EXISTS notifications (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '通知ID',
    user_id     BIGINT          NOT NULL                 COMMENT '接收人',
    type        VARCHAR(30)     NOT NULL                 COMMENT 'OVERDUE/RESERVE_READY/FINE/ANNOUNCE',
    title       VARCHAR(200)    NOT NULL                 COMMENT '标题',
    content     TEXT            DEFAULT NULL             COMMENT '内容',
    is_read     TINYINT         DEFAULT 0                COMMENT '0=未读 1=已读',
    ref_id      BIGINT          DEFAULT NULL             COMMENT '关联业务ID',
    created_at  DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_user_read (user_id, is_read),
    CONSTRAINT fk_notify_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ---------- 10. operation_logs ----------
CREATE TABLE IF NOT EXISTS operation_logs (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    user_id     BIGINT          NOT NULL                 COMMENT '操作人',
    action      VARCHAR(50)     NOT NULL                 COMMENT '操作类型',
    target_type VARCHAR(50)     DEFAULT NULL             COMMENT '操作对象类型',
    target_id   BIGINT          DEFAULT NULL             COMMENT '操作对象ID',
    detail      VARCHAR(500)    DEFAULT NULL             COMMENT '详情',
    ip          VARCHAR(45)     DEFAULT NULL             COMMENT 'IP地址',
    created_at  DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created (created_at),
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ---------- 11. token_blacklist ----------
CREATE TABLE IF NOT EXISTS token_blacklist (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    jti         VARCHAR(255)    NOT NULL                 COMMENT 'JWT ID',
    expires_at  DATETIME        NOT NULL                 COMMENT '过期时间',
    created_at  DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_jti (jti),
    KEY idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token黑名单表';

-- ---------- 12. refresh_tokens ----------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    user_id     BIGINT          NOT NULL                 COMMENT '用户',
    token       VARCHAR(255)    NOT NULL                 COMMENT 'RefreshToken',
    expires_at  DATETIME        NOT NULL                 COMMENT '过期时间',
    created_at  DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_token (token),
    KEY idx_user_id (user_id),
    KEY idx_expires (expires_at),
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RefreshToken表';


-- ================================================================
-- 阶段 2：种子数据
-- ================================================================

-- 管理员账号: admin / admin123
INSERT INTO users (username, password, real_name, role, status) VALUES
('admin', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '系统管理员', 'ADMIN', 1);

-- 示例读者
INSERT INTO users (username, password, real_name, role, status) VALUES
('reader1', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '张三', 'READER', 1);

-- 图书分类
INSERT INTO categories (id, name, parent_id, sort_order) VALUES
(1, '计算机科学', 0, 1),
(2, '文学小说',   0, 2),
(3, '历史地理',   0, 3),
(4, '自然科学',   0, 4),
(5, '社会科学',   0, 5),
(6, '编程语言',   1, 1),
(7, '数据库',     1, 2),
(8, '人工智能',   1, 3);


-- ================================================================
-- 阶段 3：测试数据
-- ================================================================

-- 额外用户
INSERT INTO users (username, password, real_name, role, phone, email, status) VALUES
('reader2', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '李四', 'READER', '13800001111', 'lisi@example.com',   1),
('reader3', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '王五', 'READER', '13800002222', 'wangwu@example.com', 1),
('reader4', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '赵六', 'READER', '13800003333', 'zhaoliu@example.com', 1),
('reader5', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '孙七', 'READER', '13800004444', 'sunqi@example.com',   1),
('reader6', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '周八', 'READER', '13800005555', 'zhouba@example.com',  0);

-- 图书
INSERT INTO books (isbn, title, author, publisher, category_id, publish_year, edition, price, total_copies, available_copies, location, description, status) VALUES
('978-7-111-72653-9', 'Java核心技术 卷I',       'Cay S. Horstmann', '机械工业出版社', 6, 2022, '第12版', 149.00, 5, 4, 'A区-3排-5层', '全面讲解Java语言核心概念与API。', 1),
('978-7-115-54473-9', '深入理解Java虚拟机',     '周志明',           '人民邮电出版社', 6, 2019, '第3版',  129.00, 3, 3, 'A区-3排-6层', 'JVM调优圣经。', 1),
('978-7-121-42493-7', 'Spring实战 第6版',       'Craig Walls',     '电子工业出版社', 6, 2022, '第6版',  128.00, 3, 3, 'A区-3排-7层', 'Spring Framework与Spring Boot实战指南。', 1),
('978-7-111-68531-5', '高性能MySQL 第4版',      'Silvia Botros',    '机械工业出版社', 7, 2021, '第4版',  139.00, 2, 2, 'A区-4排-1层', 'MySQL性能优化权威指南。', 1),
('978-7-121-38816-2', 'MySQL必知必会',          'Ben Forta',       '电子工业出版社', 7, 2020, '第1版',   49.00, 5, 5, 'A区-4排-2层', 'MySQL入门经典。', 1),
('978-7-111-64388-1', '深度学习',               'Ian Goodfellow',   '机械工业出版社', 8, 2020, '第1版',  168.00, 2, 1, 'A区-5排-3层', '深度学习领域奠基之作。', 1),
('978-7-302-58250-0', '机器学习',               '周志华',           '清华大学出版社', 8, 2021, '第2版',   88.00, 4, 4, 'A区-5排-4层', '机器学习入门经典"西瓜书"。', 1),
('978-7-02-018673-1', '活着',                   '余华',             '人民文学出版社', 2, 2012, '第3版',   38.00, 4, 4, 'B区-1排-1层', '讲述人在极端环境下生存的故事。', 1),
('978-7-5442-8019-8', '百年孤独',               '加西亚·马尔克斯',  '南海出版公司',   2, 2017, '第1版',   55.00, 2, 1, 'B区-1排-2层', '魔幻现实主义代表作。', 1),
('978-7-5302-1977-4', '人间失格',               '太宰治',           '北京十月文艺',   2, 2019, '第1版',   32.00, 2, 2, 'B区-1排-3层', '太宰治半自传体小说。', 1),
('978-7-5080-9859-5', '万历十五年',             '黄仁宇',           '中华书局',       3, 2018, '第2版',   42.00, 3, 3, 'C区-2排-4层', '剖析明朝政治制度的深层危机。', 1),
('978-7-5531-0896-5', '全球通史',               '斯塔夫里阿诺斯',   '北京大学出版社', 3, 2020, '第4版',   98.00, 2, 1, 'C区-2排-5层', '从全球视角讲述人类文明发展历程。', 1),
('978-7-5710-0918-7', '时间简史',               '史蒂芬·霍金',     '湖南科技出版社', 4, 2019, '第3版',   45.00, 3, 2, 'D区-1排-1层', '霍金关于宇宙本质的科普经典。', 1),
('978-7-5086-6397-5', '思考，快与慢',           '丹尼尔·卡尼曼',   '中信出版社',     5, 2018, '第1版',   69.00, 3, 3, 'E区-3排-2层', '行为经济学经典著作。', 1),
('978-7-100-19614-8', '乡土中国',               '费孝通',           '商务印书馆',     5, 2021, '第4版',   36.00, 3, 3, 'E区-3排-4层', '了解中国基层社会结构的经典。', 1);

-- 借阅记录
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, renew_count, status, created_at) VALUES
(2, 1, '2026-05-17 10:30:00', '2026-06-01 10:30:00', 0, 'BORROWING', '2026-05-17 10:30:00'),
(2, 8, '2026-05-09 14:00:00', '2026-06-03 14:00:00', 0, 'BORROWING', '2026-05-09 14:00:00'),
(3, 3, '2026-04-15 09:00:00', '2026-05-15 09:00:00', 0, 'RETURNED',  '2026-04-15 09:00:00'),
(4, 6, '2026-05-19 11:00:00', '2026-06-10 11:00:00', 0, 'BORROWING', '2026-05-19 11:00:00'),
(4, 9, '2026-05-14 15:00:00', '2026-06-06 15:00:00', 0, 'BORROWING', '2026-05-14 15:00:00'),
(4, 12,'2026-03-14 08:00:00', '2026-04-28 08:00:00', 1, 'BORROWING', '2026-03-14 08:00:00'),
(6, 5, '2026-05-01 13:00:00', '2026-05-31 13:00:00', 0, 'RETURNED',  '2026-05-01 13:00:00'),
(2, 13,'2026-05-27 16:45:00', '2026-06-18 16:45:00', 0, 'BORROWING', '2026-05-27 16:45:00');

-- 预约记录
INSERT INTO reservations (user_id, book_id, reserve_date, queue_position, expire_date, pickup_deadline, status, created_at) VALUES
(3, 2, '2026-05-22 09:00:00', 1, '2026-05-29 09:00:00', NULL, 'WAITING',   '2026-05-22 09:00:00'),
(4, 2, '2026-05-25 11:30:00', 2, '2026-06-01 11:30:00', NULL, 'WAITING',   '2026-05-25 11:30:00'),
(6, 2, '2026-05-28 08:00:00', 3, '2026-06-04 08:00:00', NULL, 'WAITING',   '2026-05-28 08:00:00'),
(2, 9, '2026-05-10 10:00:00', 1, '2026-05-17 10:00:00', '2026-06-01 10:00:00', 'FULFILLED', '2026-05-10 10:00:00'),
(3, 9, '2026-05-18 14:00:00', 2, '2026-05-25 14:00:00', NULL, 'WAITING',   '2026-05-18 14:00:00'),
(5, 4, '2026-05-20 16:00:00', 1, '2026-05-27 16:00:00', NULL, 'CANCELLED', '2026-05-20 16:00:00'),
(6, 13,'2026-05-01 09:00:00', 1, '2026-05-08 09:00:00', NULL, 'EXPIRED',   '2026-05-01 09:00:00');

-- 罚款
INSERT INTO fines (user_id, borrow_record_id, amount, reason, status, created_at) VALUES
(4, 6, 15.50, '逾期-全球通史(应还2026-04-28，已逾期31天)', 'UNPAID', NOW()),
(2, NULL, 69.00, '丢失-思考快与慢(原价69元赔偿)', 'PAID', '2026-05-18 09:00:00');

-- 书评
INSERT INTO book_reviews (user_id, book_id, rating, content, created_at) VALUES
(2, 8,  5, '读完泪流满面。余华用最简单的文字写出了最沉重的故事。', '2026-05-20 20:30:00'),
(3, 3,  4, '内容全面，代码示例清晰，适合有一定Java基础的读者。',    '2026-05-12 15:00:00'),
(4, 6,  5, '花书名不虚传，数学推导严谨，需要一定的基础。',          '2026-05-22 09:30:00'),
(6, 5,  4, '非常适合入门，两天就能看完，可以快速上手SQL。',         '2026-05-16 18:00:00'),
(2, 13, 4, '霍金把深奥的宇宙学讲得通俗有趣。',                    '2026-05-28 21:00:00'),
(3, 9,  5, '人名确实难记，但一旦沉浸进去，那种魔幻的命运感令人震撼。','2026-05-25 22:00:00');

-- 公告
INSERT INTO announcements (title, content, publisher_id, is_pinned, status, created_at) VALUES
('五一假期开馆时间调整通知', '各位读者：五一劳动节期间（5月1日-5月5日），图书馆开放时间调整为9:00-17:00。', 1, 1, 1, '2026-04-28 09:00:00'),
('关于逾期罚款新标准的公告', '根据学校最新规定，自2026年5月起，图书逾期罚款标准调整为每日0.50元。',     1, 1, 1, '2026-05-01 08:30:00'),
('新增电子资源数据库试用通知', '图书馆已开通IEEE Xplore数据库三个月试用期（2026.5.15-8.15）。',         1, 0, 1, '2026-05-15 10:00:00'),
('"书香校园"读书月活动启动', '6月将举办读书月活动，包括好书推荐、读书分享会、书评大赛等。',              1, 0, 1, '2026-05-26 14:00:00');

-- 通知
INSERT INTO notifications (user_id, type, title, content, is_read, ref_id, created_at) VALUES
(4, 'OVERDUE',       '图书逾期提醒',     '您借阅的《全球通史》已逾期31天，请尽快归还。',           0, 6, '2026-05-28 08:00:00'),
(2, 'RESERVE_READY', '预约到书通知',     '您预约的《百年孤独》已到馆，请在3天内到馆借阅。',           1, 4, '2026-05-28 09:00:00'),
(3, 'RESERVE_READY', '预约即将到期',     '您预约的《深入理解Java虚拟机》即将到期。',                 0, 1, '2026-05-28 10:00:00'),
(2, 'FINE',          '罚款缴纳确认',     '您缴纳的图书丢失赔偿69.00元已确认。',                     1, 2, '2026-05-20 10:05:00'),
(2, 'ANNOUNCE',      '"书香校园"读书月', '6月将举办读书月活动，欢迎参加。',                         0, 4, '2026-05-26 14:05:00'),
(3, 'ANNOUNCE',      '"书香校园"读书月', '6月将举办读书月活动，欢迎参加。',                         0, 4, '2026-05-26 14:05:00'),
(4, 'ANNOUNCE',      '"书香校园"读书月', '6月将举办读书月活动，欢迎参加。',                         0, 4, '2026-05-26 14:05:00');

-- 操作日志
INSERT INTO operation_logs (user_id, action, target_type, target_id, detail, ip, created_at) VALUES
(1, 'ADD_BOOK',          'Book',         1, '新增图书《Java核心技术 卷I》',            '127.0.0.1', '2026-05-01 09:30:00'),
(1, 'PUBLISH_ANNOUNCE',  'Announcement', 1, '发布公告"五一假期开馆时间调整通知"',      '127.0.0.1', '2026-04-28 09:00:00'),
(1, 'DISABLE_USER',      'User',         5, '禁用用户赵六(reader6)-多次逾期不还',       '127.0.0.1', '2026-05-15 16:00:00');

-- 修正库存（total_copies - 当前借出数）
UPDATE books b SET b.available_copies = b.total_copies - COALESCE(
    (SELECT COUNT(*) FROM borrow_records br WHERE br.book_id = b.id AND br.status = 'BORROWING'), 0
);


-- ================================================================
-- 阶段 4：存储过程 / 触发器 / 视图 / 函数
-- ================================================================

DELIMITER $$

-- ==================== 存储过程 ====================

DROP PROCEDURE IF EXISTS sp_borrow_book$$
CREATE PROCEDURE sp_borrow_book(
    IN  p_user_id  BIGINT, IN  p_book_id  BIGINT,
    OUT p_result   INT,    OUT p_msg      VARCHAR(200)
)
sp_label: BEGIN
    DECLARE v_status INT; DECLARE v_max_borrow INT; DECLARE v_overdue BIGINT;
    DECLARE v_current BIGINT; DECLARE v_fine BIGINT; DECLARE v_dup BIGINT;
    DECLARE v_available INT; DECLARE v_book_price DECIMAL(10,2); DECLARE v_duration INT DEFAULT 30;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; SET p_result = 99; SET p_msg = '系统异常'; END;
    START TRANSACTION;

    SELECT status, max_borrow INTO v_status, v_max_borrow FROM users WHERE id = p_user_id;
    IF v_status IS NULL OR v_status = 0 THEN ROLLBACK; SET p_result = 1; SET p_msg = '用户状态异常'; LEAVE sp_label; END IF;

    SELECT COUNT(*) INTO v_overdue FROM borrow_records WHERE user_id = p_user_id AND status = 'BORROWING' AND due_date < NOW();
    IF v_overdue > 0 THEN ROLLBACK; SET p_result = 2; SET p_msg = '存在逾期未还图书'; LEAVE sp_label; END IF;

    SELECT COUNT(*) INTO v_current FROM borrow_records WHERE user_id = p_user_id AND status = 'BORROWING';
    IF v_current >= v_max_borrow THEN ROLLBACK; SET p_result = 3; SET p_msg = CONCAT('已达最大借阅数: ', v_max_borrow); LEAVE sp_label; END IF;

    SELECT COUNT(*) INTO v_fine FROM fines WHERE user_id = p_user_id AND status = 'UNPAID';
    IF v_fine > 0 THEN ROLLBACK; SET p_result = 4; SET p_msg = '存在未缴罚款'; LEAVE sp_label; END IF;

    SELECT COUNT(*) INTO v_dup FROM borrow_records WHERE user_id = p_user_id AND book_id = p_book_id AND status = 'BORROWING';
    IF v_dup > 0 THEN ROLLBACK; SET p_result = 5; SET p_msg = '已借阅该书'; LEAVE sp_label; END IF;

    SELECT available_copies, price INTO v_available, v_book_price FROM books WHERE id = p_book_id FOR UPDATE;
    IF v_available IS NULL THEN ROLLBACK; SET p_result = 6; SET p_msg = '图书不存在'; LEAVE sp_label; END IF;
    IF v_available <= 0 THEN ROLLBACK; SET p_result = 6; SET p_msg = '无可用副本'; LEAVE sp_label; END IF;

    UPDATE books SET available_copies = available_copies - 1 WHERE id = p_book_id;
    INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status) VALUES (p_user_id, p_book_id, NOW(), DATE_ADD(NOW(), INTERVAL v_duration DAY), 'BORROWING');
    UPDATE reservations SET status = 'CANCELLED' WHERE user_id = p_user_id AND book_id = p_book_id AND status = 'WAITING';
    COMMIT; SET p_result = 0; SET p_msg = '借阅成功';
END sp_label$$


DROP PROCEDURE IF EXISTS sp_return_book$$
CREATE PROCEDURE sp_return_book(
    IN  p_borrow_id   BIGINT,
    OUT p_result      INT, OUT p_msg VARCHAR(200), OUT p_fine_amount DECIMAL(10,2)
)
sp_label: BEGIN
    DECLARE v_user_id BIGINT; DECLARE v_book_id BIGINT; DECLARE v_due_date DATETIME;
    DECLARE v_return DATETIME DEFAULT NOW(); DECLARE v_status VARCHAR(20);
    DECLARE v_days INT; DECLARE v_book_price DECIMAL(10,2); DECLARE v_fine DECIMAL(10,2);
    DECLARE v_first_id BIGINT; DECLARE v_reserve_uid BIGINT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; SET p_result = 99; SET p_msg = '系统异常'; END;
    START TRANSACTION;

    SELECT user_id, book_id, due_date, status INTO v_user_id, v_book_id, v_due_date, v_status FROM borrow_records WHERE id = p_borrow_id FOR UPDATE;
    IF v_user_id IS NULL THEN ROLLBACK; SET p_result = 1; SET p_msg = '记录不存在'; LEAVE sp_label; END IF;
    IF v_status != 'BORROWING' THEN ROLLBACK; SET p_result = 2; SET p_msg = '非借阅中'; LEAVE sp_label; END IF;

    UPDATE borrow_records SET return_date = v_return, status = 'RETURNED' WHERE id = p_borrow_id;
    SET p_fine_amount = 0;
    IF v_return > v_due_date THEN
        SET v_days = DATEDIFF(v_return, v_due_date);
        SELECT price INTO v_book_price FROM books WHERE id = v_book_id;
        SET v_fine = LEAST(v_days * 0.50, v_book_price); SET p_fine_amount = v_fine;
        INSERT INTO fines (user_id, borrow_record_id, amount, reason, status) VALUES (v_user_id, p_borrow_id, v_fine, '逾期归还', 'UNPAID');
        INSERT INTO notifications (user_id, type, title, content, ref_id) VALUES (v_user_id, 'FINE', '逾期罚款通知', CONCAT('逾期', v_days, '天，罚款', v_fine, '元'), LAST_INSERT_ID());
    END IF;

    SELECT r.id, r.user_id INTO v_first_id, v_reserve_uid FROM reservations r WHERE r.book_id = v_book_id AND r.status = 'WAITING' ORDER BY r.queue_position LIMIT 1;
    IF v_first_id IS NOT NULL THEN
        UPDATE reservations SET status = 'FULFILLED', pickup_deadline = DATE_ADD(NOW(), INTERVAL 3 DAY) WHERE id = v_first_id;
        INSERT INTO notifications (user_id, type, title, content, ref_id) VALUES (v_reserve_uid, 'RESERVE_READY', '预约到书通知', '您预约的图书已到馆，请在3天内到馆借阅', v_first_id);
    ELSE
        UPDATE books SET available_copies = available_copies + 1 WHERE id = v_book_id;
    END IF;
    COMMIT; SET p_result = 0; SET p_msg = '归还成功';
END sp_label$$


DROP PROCEDURE IF EXISTS sp_renew_book$$
CREATE PROCEDURE sp_renew_book(IN p_user_id BIGINT, IN p_borrow_id BIGINT, OUT p_result INT, OUT p_msg VARCHAR(200))
BEGIN
    DECLARE v_record_uid BIGINT; DECLARE v_book_id BIGINT; DECLARE v_due_date DATETIME;
    DECLARE v_status VARCHAR(20); DECLARE v_renew_cnt INT;
    DECLARE v_renew_days INT DEFAULT 30; DECLARE v_max_renew INT DEFAULT 2; DECLARE v_reserve BIGINT;

    SELECT user_id, book_id, due_date, status, renew_count INTO v_record_uid, v_book_id, v_due_date, v_status, v_renew_cnt FROM borrow_records WHERE id = p_borrow_id;
    IF v_book_id IS NULL OR v_record_uid != p_user_id THEN SET p_result = 1; SET p_msg = '记录不存在';
    ELSEIF v_status != 'BORROWING' THEN SET p_result = 2; SET p_msg = '仅借阅中可续借';
    ELSEIF v_due_date < NOW() THEN SET p_result = 3; SET p_msg = '已逾期不可续借';
    ELSEIF v_renew_cnt >= v_max_renew THEN SET p_result = 4; SET p_msg = '已达最大续借次数';
    ELSE
        SELECT COUNT(*) INTO v_reserve FROM reservations WHERE book_id = v_book_id AND status = 'WAITING';
        IF v_reserve > 0 THEN SET p_result = 5; SET p_msg = '有他人预约不可续借';
        ELSE
            UPDATE borrow_records SET due_date = DATE_ADD(due_date, INTERVAL v_renew_days DAY), renew_count = renew_count + 1 WHERE id = p_borrow_id;
            SET p_result = 0; SET p_msg = '续借成功';
        END IF;
    END IF;
END$$


DROP PROCEDURE IF EXISTS sp_expire_reservations$$
CREATE PROCEDURE sp_expire_reservations(OUT p_affected INT)
sp_label: BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; SET p_affected = -1; END;
    START TRANSACTION;
    UPDATE reservations SET status = 'EXPIRED' WHERE status = 'WAITING' AND expire_date < NOW();
    SET p_affected = ROW_COUNT();
    UPDATE reservations r JOIN (SELECT id, ROW_NUMBER() OVER (PARTITION BY book_id ORDER BY created_at) AS new_pos FROM reservations WHERE status = 'WAITING') sub ON r.id = sub.id SET r.queue_position = sub.new_pos;
    COMMIT;
END sp_label$$


-- ==================== 触发器 ====================

DROP TRIGGER IF EXISTS trg_borrow_before_insert$$
CREATE TRIGGER trg_borrow_before_insert BEFORE INSERT ON borrow_records FOR EACH ROW
BEGIN
    DECLARE v_available INT;
    SELECT available_copies INTO v_available FROM books WHERE id = NEW.book_id;
    IF v_available IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '图书不存在'; END IF;
    IF v_available <= 0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '该书暂无可借副本'; END IF;
END$$


DROP TRIGGER IF EXISTS trg_reservation_after_update$$
CREATE TRIGGER trg_reservation_after_update AFTER UPDATE ON reservations FOR EACH ROW
BEGIN
    IF OLD.status = 'WAITING' AND NEW.status = 'FULFILLED' THEN
        INSERT INTO notifications (user_id, type, title, content, ref_id)
        VALUES (NEW.user_id, 'RESERVE_READY', '预约到书通知',
                CONCAT('您预约的图书已到馆，请在', DATE_FORMAT(NEW.pickup_deadline, '%Y-%m-%d %H:%i'), '前到馆借阅'), NEW.id);
    END IF;
END$$


DROP TRIGGER IF EXISTS trg_book_review_check$$
CREATE TRIGGER trg_book_review_check BEFORE INSERT ON book_reviews FOR EACH ROW
BEGIN
    DECLARE v_cnt INT;
    SELECT COUNT(*) INTO v_cnt FROM borrow_records WHERE user_id = NEW.user_id AND book_id = NEW.book_id;
    IF v_cnt = 0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '您未借阅过该书，无法评论'; END IF;
END$$


-- ==================== 函数 ====================

DROP FUNCTION IF EXISTS fn_calc_fine$$
CREATE FUNCTION fn_calc_fine(p_due_date DATETIME, p_return_date DATETIME, p_book_price DECIMAL(10,2))
RETURNS DECIMAL(10,2) DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_days INT; DECLARE v_fine DECIMAL(10,2);
    IF p_return_date IS NULL OR p_return_date <= p_due_date THEN RETURN 0; END IF;
    SET v_days = DATEDIFF(p_return_date, p_due_date);
    SET v_fine = LEAST(v_days * 0.50, p_book_price);
    RETURN v_fine;
END$$


DROP FUNCTION IF EXISTS fn_can_borrow$$
CREATE FUNCTION fn_can_borrow(p_user_id BIGINT) RETURNS VARCHAR(200) READS SQL DATA
BEGIN
    DECLARE v_status INT; DECLARE v_max_borrow INT; DECLARE v_overdue BIGINT;
    DECLARE v_current BIGINT; DECLARE v_fine BIGINT;
    SELECT status, max_borrow INTO v_status, v_max_borrow FROM users WHERE id = p_user_id;
    IF v_status IS NULL THEN RETURN '用户不存在'; END IF;
    IF v_status = 0 THEN RETURN '账号已被禁用'; END IF;
    SELECT COUNT(*) INTO v_overdue FROM borrow_records WHERE user_id = p_user_id AND status = 'BORROWING' AND due_date < NOW();
    IF v_overdue > 0 THEN RETURN '存在逾期未还图书'; END IF;
    SELECT COUNT(*) INTO v_current FROM borrow_records WHERE user_id = p_user_id AND status = 'BORROWING';
    IF v_current >= v_max_borrow THEN RETURN '已达最大借阅数'; END IF;
    SELECT COUNT(*) INTO v_fine FROM fines WHERE user_id = p_user_id AND status = 'UNPAID';
    IF v_fine > 0 THEN RETURN '存在未缴罚款'; END IF;
    RETURN '';
END$$


DELIMITER ;

-- ==================== 视图 ====================

DROP VIEW IF EXISTS v_borrow_detail;
CREATE VIEW v_borrow_detail AS
SELECT br.id, br.user_id, u.real_name AS user_name, br.book_id, b.title AS book_title, b.author, b.isbn,
       br.borrow_date, br.due_date, br.return_date, br.renew_count, br.status,
       DATEDIFF(br.due_date, NOW()) AS days_remaining
FROM borrow_records br
JOIN users u ON br.user_id = u.id
JOIN books b ON br.book_id = b.id;

DROP VIEW IF EXISTS v_overdue_books;
CREATE VIEW v_overdue_books AS
SELECT br.id, u.real_name AS user_name, u.phone, b.title AS book_title, b.author,
       br.borrow_date, br.due_date,
       DATEDIFF(NOW(), br.due_date) AS overdue_days,
       LEAST(DATEDIFF(NOW(), br.due_date) * 0.50, b.price) AS estimated_fine
FROM borrow_records br JOIN users u ON br.user_id = u.id JOIN books b ON br.book_id = b.id
WHERE br.status = 'BORROWING' AND br.due_date < NOW() ORDER BY overdue_days DESC;

DROP VIEW IF EXISTS v_reservation_queue;
CREATE VIEW v_reservation_queue AS
SELECT r.id, r.book_id, b.title AS book_title, b.author, r.user_id, u.real_name AS user_name,
       r.queue_position, r.reserve_date, r.expire_date, r.pickup_deadline, r.status
FROM reservations r JOIN books b ON r.book_id = b.id JOIN users u ON r.user_id = u.id
ORDER BY r.book_id, r.queue_position;

DROP VIEW IF EXISTS v_book_rating;
CREATE VIEW v_book_rating AS
SELECT b.id AS book_id, b.title, b.author, b.publisher,
       COUNT(rv.id) AS review_count, ROUND(IFNULL(AVG(rv.rating), 0), 1) AS avg_rating,
       COUNT(CASE WHEN rv.rating = 5 THEN 1 END) AS five_star,
       COUNT(CASE WHEN rv.rating = 4 THEN 1 END) AS four_star,
       COUNT(CASE WHEN rv.rating <= 3 THEN 1 END) AS low_star
FROM books b LEFT JOIN book_reviews rv ON b.id = rv.book_id
GROUP BY b.id, b.title, b.author, b.publisher;
