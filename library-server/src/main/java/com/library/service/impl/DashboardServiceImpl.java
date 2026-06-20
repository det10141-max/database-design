package com.library.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.dto.response.DashboardResponse;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final UserMapper userMapper;
    private final FineMapper fineMapper;
    private final ReservationMapper reservationMapper;

    @Override
    public DashboardResponse stats() {
        long totalBooks = bookMapper.selectCount(null);
        long borrowedCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus,"BORROWING"));
        long overdueCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus,"BORROWING").lt(BorrowRecord::getDueDate, LocalDateTime.now()));
        long todayBorrow = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>().ge(BorrowRecord::getBorrowDate, LocalDate.now().atStartOfDay()));
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getIsDeleted,0));
        long unpaidFines = fineMapper.selectCount(new LambdaQueryWrapper<Fine>().eq(Fine::getStatus,"UNPAID"));
        long activeReservations = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>().eq(Reservation::getStatus,"WAITING"));
        long totalPendingFine = fineMapper.selectTotalUnpaid();
        return new DashboardResponse(totalBooks, borrowedCount, overdueCount, todayBorrow, totalUsers, unpaidFines, activeReservations, totalPendingFine);
    }
}
