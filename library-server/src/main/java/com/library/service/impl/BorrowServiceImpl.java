package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.config.AppProperties;
import com.library.dto.response.BorrowRecordVO;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.BorrowService;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BorrowDetailMapper borrowDetailMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final ReservationMapper reservationMapper;
    private final FineMapper fineMapper;
    private final NotificationService notificationService;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void borrow(Long userId, Long bookId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException("用户状态异常");
        }

        // 校验是否还有逾期未还
        Long overdueCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, userId)
                        .eq(BorrowRecord::getStatus, "BORROWING")
                        .lt(BorrowRecord::getDueDate, LocalDateTime.now()));
        if (overdueCount > 0) {
            throw new BusinessException("存在逾期未还图书，请先归还");
        }

        // 校验已达借阅上限
        Long currentCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, userId)
                        .eq(BorrowRecord::getStatus, "BORROWING"));
        if (currentCount >= user.getMaxBorrow()) {
            throw new BusinessException("已到达最大借阅数: " + user.getMaxBorrow());
        }

        // 校验是否有未缴罚款
        Long fineCount = fineMapper.selectCount(
                new LambdaQueryWrapper<Fine>()
                        .eq(Fine::getUserId, userId)
                        .eq(Fine::getStatus, "UNPAID"));
        if (fineCount > 0) {
            throw new BusinessException("存在未缴罚款，请先缴纳");
        }

        // 是否已借同一本书
        Long dup = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, userId)
                        .eq(BorrowRecord::getBookId, bookId)
                        .eq(BorrowRecord::getStatus, "BORROWING"));
        if (dup > 0) {
            throw new BusinessException("您已借阅该书，不可重复借阅");
        }

        // 行级锁防并发超借
        Book book = bookMapper.selectByIdForUpdate(bookId);
        if (book == null || book.getStatus() == 0) {
            throw new BusinessException("图书不存在或已下架");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException("该书暂无可借副本，请预约");
        }

        // 扣减库存并更新借阅记录（同一事务内）
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookMapper.updateById(book);

        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(appProperties.getBorrow().getDurationDays()));
        record.setStatus("BORROWING");
        borrowRecordMapper.insert(record);

        // 借阅成功通知用户
        notificationService.create(userId, "BORROW",
                "借阅成功通知",
                "您已成功借阅《" + book.getTitle() + "》，应还日期：" +
                        record.getDueDate().toLocalDate() + "，请按时归还",
                record.getId());
    }

    @Override
    @Transactional
    public void returnBook(Long borrowId) {
        // 行级锁查询，防止并发下多次归还导致 availableCopies 超过 totalCopies
        BorrowRecord record = borrowRecordMapper.selectByIdForUpdate(borrowId);
        if (record == null || !"BORROWING".equals(record.getStatus())) {
            throw new BusinessException("该记录不是借阅中状态");
        }

        record.setReturnDate(LocalDateTime.now());
        record.setStatus("RETURNED");
        borrowRecordMapper.updateById(record);

        // 检查预约队列
        List<Reservation> reservations = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getBookId, record.getBookId())
                        .eq(Reservation::getStatus, "WAITING")
                        .orderByAsc(Reservation::getQueuePosition));

        if (!reservations.isEmpty()) {
            // 有预约：库存锁定给第一位预约者
            Reservation first = reservations.get(0);
            first.setStatus("FULFILLED");
            first.setPickupDeadline(LocalDateTime.now().plusDays(appProperties.getReserve().getPickupDays()));
            reservationMapper.updateById(first);
            notificationService.create(first.getUserId(), "RESERVE_READY",
                    "预约书已到馆", "您预约的图书已到馆，请于3天内到馆借阅", first.getId());

            // 后续等待者排队位置前移一位
            for (int i = 1; i < reservations.size(); i++) {
                Reservation r = reservations.get(i);
                r.setQueuePosition(r.getQueuePosition() - 1);
                reservationMapper.updateById(r);
            }
        } else {
            // 无预约：归还公共库存（行级锁保证 availableCopies 不会超过 totalCopies）
            Book book = bookMapper.selectByIdForUpdate(record.getBookId());
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookMapper.updateById(book);
        }

        // 逾期罚款
        if (record.getReturnDate().isAfter(record.getDueDate())) {
            long days = record.getReturnDate().toLocalDate().toEpochDay()
                    - record.getDueDate().toLocalDate().toEpochDay();
            double rate = appProperties.getFine().getDailyRate();
            Book book = bookMapper.selectById(record.getBookId());
            double amount = Math.min(days * rate, book.getPrice().doubleValue());
            Fine fine = new Fine();
            fine.setUserId(record.getUserId());
            fine.setBorrowRecordId(record.getId());
            fine.setAmount(java.math.BigDecimal.valueOf(amount));
            fine.setReason("逾期归还");
            fineMapper.insert(fine);
            notificationService.create(record.getUserId(), "FINE",
                    "逾期罚款通知", "您有逾期图书已产生罚款" + amount + "元", fine.getId());
        }
    }

    @Override
    @Transactional
    public void renew(Long userId, Long borrowId) {
        BorrowRecord record = borrowRecordMapper.selectById(borrowId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("借阅记录不存在");
        }
        if (!"BORROWING".equals(record.getStatus())) {
            throw new BusinessException("仅借阅中状态可续借");
        }
        if (record.getDueDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("已逾期，不可续借");
        }
        if (record.getRenewCount() >= appProperties.getBorrow().getMaxRenew()) {
            throw new BusinessException("已达到最大续借次数");
        }

        // 检查是否有人预约
        Long reserveCount = reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getBookId, record.getBookId())
                        .eq(Reservation::getStatus, "WAITING"));
        if (reserveCount > 0) {
            throw new BusinessException("该书有他人预约，不可续借");
        }

        record.setDueDate(record.getDueDate().plusDays(appProperties.getBorrow().getRenewDays()));
        record.setRenewCount(record.getRenewCount() + 1);
        borrowRecordMapper.updateById(record);
    }

    @Override
    @Transactional
    public void markLost(Long borrowId) {
        // 行级锁查询 + 状态校验，防止多次点击"丢失"导致 totalCopies 被重复扣减
        BorrowRecord record = borrowRecordMapper.selectByIdForUpdate(borrowId);
        if (record == null) throw new BusinessException("借阅记录不存在");
        if (!"BORROWING".equals(record.getStatus())) {
            throw new BusinessException("该记录不是借阅中状态，无法标记丢失");
        }

        record.setStatus("LOST");
        borrowRecordMapper.updateById(record);

        // 标记丢失：借出时 availableCopies 已扣减，此处仅需扣减 totalCopies
        // 行级锁保证 totalCopies 不会被并发重复扣减
        Book book = bookMapper.selectByIdForUpdate(record.getBookId());
        book.setTotalCopies(book.getTotalCopies() - 1);
        bookMapper.updateById(book);

        Fine fine = new Fine();
        fine.setUserId(record.getUserId());
        fine.setBorrowRecordId(record.getId());
        fine.setAmount(book.getPrice());
        fine.setReason("图书丢失");
        fineMapper.insert(fine);

        notificationService.create(record.getUserId(), "FINE",
                "图书丢失罚款", "您丢失的图书《" + book.getTitle() + "》需缴纳罚款" + book.getPrice() + "元", fine.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public IPage<BorrowRecordVO> pageByUser(Long userId, int page, int pageSize, String status) {
        LambdaQueryWrapper<com.library.dto.response.BorrowDetailVO> qw = new LambdaQueryWrapper<>();
        qw.eq(com.library.dto.response.BorrowDetailVO::getUserId, userId);
        if (status != null && !status.isEmpty()) qw.eq(com.library.dto.response.BorrowDetailVO::getStatus, status);
        qw.orderByDesc(com.library.dto.response.BorrowDetailVO::getId);
        IPage<com.library.dto.response.BorrowDetailVO> p = borrowDetailMapper.selectPageView(new Page<>(page, pageSize), qw);
        return convertPage(p);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IPage<BorrowRecordVO> pageAll(int page, int pageSize, String status) {
        LambdaQueryWrapper<com.library.dto.response.BorrowDetailVO> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) qw.eq(com.library.dto.response.BorrowDetailVO::getStatus, status);
        qw.orderByDesc(com.library.dto.response.BorrowDetailVO::getId);
        IPage<com.library.dto.response.BorrowDetailVO> p = borrowDetailMapper.selectPageView(new Page<>(page, pageSize), qw);
        return convertPage(p);
    }

    private IPage<BorrowRecordVO> convertPage(IPage<com.library.dto.response.BorrowDetailVO> src) {
        IPage<BorrowRecordVO> dst = new Page<>(src.getCurrent(), src.getSize(), src.getTotal());
        dst.setRecords(src.getRecords().stream().map(d -> {
            BorrowRecordVO vo = new BorrowRecordVO();
            vo.setId(d.getId());
            vo.setUserId(d.getUserId());
            vo.setBookId(d.getBookId());
            vo.setBookTitle(d.getBookTitle());
            vo.setBorrowDate(d.getBorrowDate());
            vo.setDueDate(d.getDueDate());
            vo.setReturnDate(d.getReturnDate());
            vo.setRenewCount(d.getRenewCount());
            vo.setStatus(d.getStatus());
            return vo;
        }).collect(java.util.stream.Collectors.toList()));
        return dst;
    }

    @Override
    public String checkEligibility(Long userId) {
        return bookMapper.checkBorrowEligibility(userId);
    }
}
