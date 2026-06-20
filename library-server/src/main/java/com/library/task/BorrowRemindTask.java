package com.library.task;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.BorrowRecord;
import com.library.mapper.BorrowRecordMapper;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BorrowRemindTask {
    private final BorrowRecordMapper borrowRecordMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void remindReturn() {
        log.info("BorrowRemindTask start");
        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> list = borrowRecordMapper.selectList(
            new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getStatus, "BORROWING")
                .between(BorrowRecord::getDueDate, now, now.plusDays(3)));
        for (BorrowRecord r : list) {
            long days = r.getDueDate().toLocalDate().toEpochDay() - now.toLocalDate().toEpochDay();
            notificationService.create(r.getUserId(), "OVERDUE_REMIND", "借阅即将到期", "您有图书将在" + days + "天后到期，请及时归还", r.getId());
        }
        log.info("BorrowRemindTask done, reminded {}", list.size());
    }
}
