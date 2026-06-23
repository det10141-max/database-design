# 修复记录

## 1. 修复可借数量异常和防重复提交问题

**日期:** 2026-06-20  
**标签:** 0.1.0

### 问题描述

1. `/reader/home` 页面书籍可借数量异常显示（可借 > 总数量，如"可借 2 / 1"）
2. `/admin/fines` 和 `/admin/borrows` 页面多次点击操作按钮导致重复提交

### 根因分析

- **可借数量异常：** `BorrowServiceImpl.markLost()` 方法缺少借阅记录状态校验，多次点击"丢失"会重复执行 `totalCopies - 1`，而 `availableCopies` 不变，导致 `availableCopies > totalCopies`
- **重复提交：** 前端按钮无 loading/禁用状态；后端 `markLost` 无状态检查，`returnBook`/`pay` 无行级锁，并发下状态检查被绕过

### 修复内容

**后端：**

- `BorrowServiceImpl.markLost()` — 添加状态校验（仅 `BORROWING` 状态可标记丢失）+ `selectByIdForUpdate` 行级锁
- `BorrowServiceImpl.returnBook()` — `selectById` 改为 `selectByIdForUpdate` 行级锁 + 库存恢复用行级锁
- `FineServiceImpl.pay()` — `selectById` 改为 `selectByIdForUpdate` 行级锁 + 添加 `@Transactional`
- `BookServiceImpl` — 新增 `sanitizeCopies()` 方法，在 `page()` 和 `detail()` 查询时校验 `availableCopies ∈ [0, totalCopies]`
- `BorrowRecordMapper` — 新增 `selectByIdForUpdate()` 行级锁方法
- `FineMapper` — 新增 `selectByIdForUpdate()` 行级锁方法

**前端：**

- `BorrowListView.vue` — 按钮添加 `loading`/`disabled` 状态 + `actingId`/`actingType` 状态锁
- `FineListView.vue` — 按钮添加 `loading`/`disabled` 状态 + `payingId` 状态锁
- `HomeView.vue` — 新增 `safeAvailable()` 前端数据校验函数

**涉及文件：**

- `library-server/src/main/java/com/library/mapper/BorrowRecordMapper.java`
- `library-server/src/main/java/com/library/mapper/FineMapper.java`
- `library-server/src/main/java/com/library/service/impl/BorrowServiceImpl.java`
- `library-server/src/main/java/com/library/service/impl/FineServiceImpl.java`
- `library-server/src/main/java/com/library/service/impl/BookServiceImpl.java`
- `library-web/src/views/admin/BorrowListView.vue`
- `library-web/src/views/admin/FineListView.vue`
- `library-web/src/views/reader/HomeView.vue`

---

## 2. 修复借书 500 错误 — 数据库触发器与 Java 层执行顺序冲突

**日期:** 2026-06-21

### 问题描述

用户界面借书时返回 `POST /api/reader/borrows 500 (Internal Server Error)`，错误信息：

```
java.sql.SQLException: 该书暂无可借副本
```

### 根因分析

`trg_borrow_before_insert` 触发器与 `BorrowServiceImpl.borrow()` 在同一事务内的执行顺序冲突：

```
同一事务内执行顺序：
1. Java: selectByIdForUpdate(bookId)   → availableCopies = 1
2. Java: 检查 availableCopies <= 0     → 1 > 0，放行
3. Java: availableCopies = 1-1 = 0, updateById() → DB 中 availableCopies = 0
4. Java: INSERT borrow_records         → 触发器触发
5. 触发器: SELECT available_copies     → 读到 0（第3步刚扣减的）
6. 触发器: IF v_available <= 0         → 0 <= 0，SIGNAL 报错！
```

Java 层先扣库存到 0，再插入借阅记录；触发器此时再读库存发现已经是 0，将正常的"最后一本借出"误判为无库存，导致每次借最后一本书时必失败。

### 修复内容

**文件：** `library-server/src/main/resources/db/sql-objects/02_triggers.sql`

将触发器条件从 `v_available <= 0` 改为 `v_available < 0`，只拦截库存为负数的真正异常情况，不再误拦 `availableCopies = 0`（Java 层已正确扣减的结果）。

**修改前：**

```sql
IF v_available <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '该书暂无可借副本，无法借阅';
END IF;
```

**修改后：**

```sql
IF v_available < 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '该书暂无可借副本，无法借阅';
END IF;
```

**数据库更新：** 需在 MySQL 中重新执行触发器创建语句（DROP + CREATE）使修改生效。

### 防护机制说明

- Java 层已用 `selectByIdForUpdate` 行级锁 + `availableCopies <= 0` 校验做并发控制
- 触发器的职责降级为兜底防护：仅拦截库存异常变为负数的情况
