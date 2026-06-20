package com.library.task;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueCheckTask {
    private final BorrowRecordMapper borrowRecordMapper;
    private final FineMapper fineMapper;
    private final BookMapper bookMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void checkOverdue() {
        log.info("OverdueCheckTask start");
        List<BorrowRecord> overdue = borrowRecordMapper.selectList(
            new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getStatus, "BORROWING")
                .lt(BorrowRecord::getDueDate, LocalDateTime.now()));
        for (BorrowRecord r : overdue) {
            Long exist = fineMapper.selectCount(
                new LambdaQueryWrapper<Fine>()
                    .eq(Fine::getBorrowRecordId, r.getId())
                    .eq(Fine::getReason, "逾期归还"));
            if (exist == 0) {
                Book book = bookMapper.selectById(r.getBookId());
                long days = LocalDateTime.now().toLocalDate().toEpochDay() - r.getDueDate().toLocalDate().toEpochDay();
                double amount = Math.min(days * 0.5, book.getPrice().doubleValue());
                Fine fine = new Fine();
                fine.setUserId(r.getUserId());
                fine.setBorrowRecordId(r.getId());
                fine.setAmount(BigDecimal.valueOf(amount));
                fine.setReason("逾期归还");
                fineMapper.insert(fine);
                notificationService.create(r.getUserId(), "OVERDUE", "逾期提醒", "您的借阅已逾期" + days + "天", r.getId());
            }
        }
        log.info("OverdueCheckTask done, processed {}", overdue.size());
    }
}
