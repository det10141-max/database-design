-- ================================================================
-- 图书管理系统 — 数据库初始化脚本
-- 版本: v1.0 | 数据库: MySQL 8.0+ | 引擎: InnoDB
-- 字符集: utf8mb4 | 排序规则: utf8mb4_unicode_ci
-- ================================================================

CREATE DATABASE IF NOT EXISTS library_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE library_db;

-- ================================================================
-- 1. users — 用户表
-- ================================================================
CREATE TABLE users (
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

-- ================================================================
-- 2. categories — 图书分类表
-- ================================================================
CREATE TABLE categories (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    name        VARCHAR(50)     NOT NULL                 COMMENT '分类名称',
    parent_id   BIGINT          DEFAULT 0                COMMENT '上级分类ID，0=顶级',
    sort_order  INT             DEFAULT 0                COMMENT '排序',
    created_at  DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- ================================================================
-- 3. books — 图书表
-- ================================================================
CREATE TABLE books (
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
    available_copies INT             NOT NULL                 COMMENT '当前可借册数（公共库存）',
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

-- ================================================================
-- 4. borrow_records — 借阅记录表
-- ================================================================
CREATE TABLE borrow_records (
    id           BIGINT                                NOT NULL AUTO_INCREMENT  COMMENT '借阅ID',
    user_id      BIGINT                                NOT NULL                 COMMENT '借阅人',
    book_id      BIGINT                                NOT NULL                 COMMENT '图书',
    borrow_date  DATETIME                              NOT NULL                 COMMENT '借出日期',
    due_date     DATETIME                              NOT NULL                 COMMENT '应还日期',
    return_date  DATETIME                              DEFAULT NULL             COMMENT '实际归还日期',
    renew_count  INT                                   DEFAULT 0                COMMENT '续借次数(上限2)',
    status       ENUM('BORROWING','RETURNED','LOST')   NOT NULL                 COMMENT '状态',
    created_at   DATETIME                              DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_status (status),
    CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- ================================================================
-- 5. reservations — 预约表
-- ================================================================
CREATE TABLE reservations (
    id               BIGINT                                                NOT NULL AUTO_INCREMENT  COMMENT '预约ID',
    user_id          BIGINT                                                NOT NULL                 COMMENT '用户',
    book_id          BIGINT                                                NOT NULL                 COMMENT '图书',
    reserve_date     DATETIME                                              NOT NULL                 COMMENT '预约时间',
    queue_position   INT                                                   DEFAULT 0                COMMENT '排队序号',
    expire_date      DATETIME                                              DEFAULT NULL             COMMENT '预约失效时间（7天）',
    pickup_deadline  DATETIME                                              DEFAULT NULL             COMMENT '到书保留截止时间（3天）',
    status           ENUM('WAITING','FULFILLED','CANCELLED','EXPIRED','BORROWED')  NOT NULL  COMMENT '状态',
    created_at       DATETIME                                              DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_status (status),
    CONSTRAINT fk_reserve_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reserve_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- ================================================================
-- 6. fines — 罚款表
-- ================================================================
CREATE TABLE fines (
    id               BIGINT                        NOT NULL AUTO_INCREMENT  COMMENT '罚款ID',
    user_id          BIGINT                        NOT NULL                 COMMENT '用户',
    borrow_record_id BIGINT                        DEFAULT NULL             COMMENT '关联借阅记录（损坏/丢失可为空）',
    amount           DECIMAL(10,2)                 NOT NULL                 COMMENT '罚款金额',
    reason           VARCHAR(255)                  DEFAULT NULL             COMMENT '原因（逾期/损坏/丢失）',
    status           ENUM('UNPAID','PAID')         DEFAULT 'UNPAID'         COMMENT '状态',
    paid_at          DATETIME                      DEFAULT NULL             COMMENT '缴纳时间',
    created_at       DATETIME                      DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_borrow_record_id (borrow_record_id),
    CONSTRAINT fk_fine_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_fine_borrow FOREIGN KEY (borrow_record_id) REFERENCES borrow_records(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='罚款表';

-- ================================================================
-- 7. book_reviews — 书评表
-- ================================================================
CREATE TABLE book_reviews (
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

-- ================================================================
-- 8. announcements — 公告表
-- ================================================================
CREATE TABLE announcements (
    id           BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '公告ID',
    title        VARCHAR(200)    NOT NULL                 COMMENT '标题',
    content      TEXT            DEFAULT NULL             COMMENT '内容',
    publisher_id BIGINT          NOT NULL                 COMMENT '发布人(管理员)',
    is_pinned    TINYINT         DEFAULT 0                COMMENT '置顶',
    status       TINYINT         DEFAULT 1                COMMENT '1=发布 0=草稿',
    publish_at   DATETIME        DEFAULT NULL             COMMENT '定时发布时间，NULL=立即发布',
    created_at   DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    updated_at   DATETIME        DEFAULT NOW() ON UPDATE NOW() COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_publisher (publisher_id),
    KEY idx_status (status),
    CONSTRAINT fk_announce_publisher FOREIGN KEY (publisher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ================================================================
-- 9. notifications — 通知表
-- ================================================================
CREATE TABLE notifications (
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

-- ================================================================
-- 10. operation_logs — 操作日志表
-- ================================================================
CREATE TABLE operation_logs (
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

-- ================================================================
-- 11. token_blacklist — Token 黑名单表（用于登出）
-- ================================================================
CREATE TABLE token_blacklist (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    jti         VARCHAR(255)    NOT NULL                 COMMENT 'JWT ID',
    expires_at  DATETIME        NOT NULL                 COMMENT '过期时间（用于清理）',
    created_at  DATETIME        DEFAULT NOW()            COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_jti (jti),
    KEY idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token黑名单表';

-- ================================================================
-- 12. refresh_tokens — RefreshToken 表
-- ================================================================
CREATE TABLE refresh_tokens (
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
-- 初始数据
-- ================================================================

-- 管理员账号: admin / admin123（BCrypt 加密）
INSERT INTO users (username, password, real_name, role, status)
VALUES ('admin', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '系统管理员', 'ADMIN', 1);

-- 示例读者
INSERT INTO users (username, password, real_name, role, status)
VALUES ('reader1', '$2a$10$K.AldWxTzYmmWMWjX3t5BuE43KPyHqGQHbGWecKmMef9.5gY3q.Ka', '张三', 'READER', 1);

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
