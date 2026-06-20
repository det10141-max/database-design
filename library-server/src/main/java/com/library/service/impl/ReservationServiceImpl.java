package com.library.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.config.AppProperties;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.NotificationService;
import com.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationMapper reservationMapper;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final UserMapper userMapper;
    private final FineMapper fineMapper;
    private final NotificationService notificationService;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void reserve(Long userId, Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        // 有可借副本时不应预约，引导用户直接借阅
        if (book.getAvailableCopies() > 0) {
            throw new BusinessException("该书有可借副本，请直接借阅");
        }
        // 重复预约校验
        if (reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(Reservation::getBookId, bookId)
                .eq(Reservation::getStatus, "WAITING")) > 0) {
            throw new BusinessException("您已预约该书");
        }

        // 计算排队位置（当前等待中的数量 + 1）
        Long cnt = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getBookId, bookId)
                .eq(Reservation::getStatus, "WAITING"));

        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setBookId(bookId);
        r.setReserveDate(LocalDateTime.now());
        r.setExpireDate(LocalDateTime.now().plusDays(appProperties.getReserve().getExpireDays()));
        r.setStatus("WAITING");
        r.setQueuePosition(cnt.intValue() + 1);
        reservationMapper.insert(r);

        // 预约成功通知
        notificationService.create(userId, "RESERVE",
                "预约成功通知",
                "您已成功预约《" + book.getTitle() + "》，当前排队位置：" + r.getQueuePosition(),
                r.getId());
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long reservationId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null || !r.getUserId().equals(userId)) {
            throw new BusinessException("预约记录不存在");
        }
        if (!"WAITING".equals(r.getStatus())) {
            throw new BusinessException("仅等待中的预约可取消");
        }

        Integer cancelledPosition = r.getQueuePosition();
        r.setStatus("CANCELLED");
        reservationMapper.updateById(r);

        // 取消后，后续预约者排队位置前移一位
        reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getBookId, r.getBookId())
                .eq(Reservation::getStatus, "WAITING")
                .gt(Reservation::getQueuePosition, cancelledPosition)
                .orderByAsc(Reservation::getQueuePosition))
            .forEach(next -> {
                next.setQueuePosition(next.getQueuePosition() - 1);
                reservationMapper.updateById(next);
            });
    }

    /**
     * 预约到书后，用户来馆取书借阅。
     * 注意：库存已在归还时锁定给预约者，此处不再扣减库存，仅创建借阅记录。
     */
    @Override
    @Transactional
    public void fulfill(Long reservationId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) {
            throw new BusinessException("预约不存在");
        }
        if (!"FULFILLED".equals(r.getStatus())) {
            throw new BusinessException("预约未到馆，无法借阅");
        }
        if (r.getPickupDeadline() != null && r.getPickupDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("已超过取书保留期限");
        }

        // 借阅资格校验（与正常借阅保持一致）
        User user = userMapper.selectById(r.getUserId());
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException("用户状态异常");
        }
        Long overdueCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, r.getUserId())
                        .eq(BorrowRecord::getStatus, "BORROWING")
                        .lt(BorrowRecord::getDueDate, LocalDateTime.now()));
        if (overdueCount > 0) {
            throw new BusinessException("存在逾期未还图书，请先归还");
        }
        Long currentCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, r.getUserId())
                        .eq(BorrowRecord::getStatus, "BORROWING"));
        if (currentCount >= user.getMaxBorrow()) {
            throw new BusinessException("已到达最大借阅数: " + user.getMaxBorrow());
        }
        Long fineCount = fineMapper.selectCount(
                new LambdaQueryWrapper<Fine>()
                        .eq(Fine::getUserId, r.getUserId())
                        .eq(Fine::getStatus, "UNPAID"));
        if (fineCount > 0) {
            throw new BusinessException("存在未缴罚款，请先缴纳");
        }

        // 库存已在归还时锁定给该预约，此处直接创建借阅记录
        BorrowRecord br = new BorrowRecord();
        br.setUserId(r.getUserId());
        br.setBookId(r.getBookId());
        br.setBorrowDate(LocalDateTime.now());
        br.setDueDate(LocalDateTime.now().plusDays(appProperties.getBorrow().getDurationDays()));
        br.setStatus("BORROWING");
        borrowRecordMapper.insert(br);

        // 预约状态更新为已完成（复用 FULFILLED 表示已取书）
        // 这里不改变状态，因为 FULFILLED 已表示到书，取书后可标记完成
        // 为区分"到书待取"和"已取书"，引入新状态 BORROWED
        r.setStatus("BORROWED");
        reservationMapper.updateById(r);

        Book book = bookMapper.selectById(r.getBookId());
        notificationService.create(r.getUserId(), "BORROW",
                "预约取书成功",
                "您已成功取书《" + book.getTitle() + "》，应还日期：" + br.getDueDate().toLocalDate(),
                br.getId());
    }

    @Override
    public List<Reservation> listByUser(Long userId) {
        List<Reservation> list = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getCreatedAt));
        fillBookTitle(list);
        return list;
    }

    @Override
    public Page<Reservation> pageAll(int page, int pageSize, String status) {
        LambdaQueryWrapper<Reservation> qw = new LambdaQueryWrapper<Reservation>()
                .orderByDesc(Reservation::getCreatedAt);
        if (status != null && !status.isEmpty()) {
            qw.eq(Reservation::getStatus, status);
        }
        Page<Reservation> p = reservationMapper.selectPage(new Page<>(page, pageSize), qw);
        // 批量填充书名和用户名
        if (!p.getRecords().isEmpty()) {
            fillBookTitle(p.getRecords());
            java.util.Set<Long> userIds = p.getRecords().stream()
                    .map(Reservation::getUserId).collect(java.util.stream.Collectors.toSet());
            java.util.Map<Long, String> usernameMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(java.util.stream.Collectors.toMap(User::getId, User::getUsername));
            p.getRecords().forEach(r -> r.setUsername(usernameMap.get(r.getUserId())));
        }
        return p;
    }

    /** 批量填充书名，避免 N+1 查询 */
    private void fillBookTitle(List<Reservation> list) {
        if (list.isEmpty()) return;
        java.util.Set<Long> bookIds = list.stream()
                .map(Reservation::getBookId).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, String> bookTitleMap = bookMapper.selectBatchIds(bookIds).stream()
                .collect(java.util.stream.Collectors.toMap(Book::getId, Book::getTitle));
        list.forEach(r -> r.setBookTitle(bookTitleMap.get(r.getBookId())));
    }
}
