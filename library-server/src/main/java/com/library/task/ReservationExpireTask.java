package com.library.task;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.*;
import com.library.mapper.*;
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
public class ReservationExpireTask {
    private final ReservationMapper reservationMapper;
    private final BookMapper bookMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void expireReservations() {
        log.info("ReservationExpireTask start");
        // 预约过期 (7天未到书)
        List<Reservation> waiting = reservationMapper.selectList(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, "WAITING")
                .lt(Reservation::getExpireDate, LocalDateTime.now()));
        for (Reservation r : waiting) {
            r.setStatus("EXPIRED");
            reservationMapper.updateById(r);
            notificationService.create(r.getUserId(), "ANNOUNCE", "预约已过期", "您的预约已超7天未到书，已自动取消", r.getId());
            // 后续等待者排队位置前移一位
            shiftQueuePosition(r.getBookId(), r.getQueuePosition());
        }
        // 到书保留期超时 (已通知但未来借)
        List<Reservation> fulfilled = reservationMapper.selectList(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, "FULFILLED")
                .lt(Reservation::getPickupDeadline, LocalDateTime.now()));
        for (Reservation r : fulfilled) {
            r.setStatus("EXPIRED");
            reservationMapper.updateById(r);
            // 释放库存给下一个预约者或公共池
            List<Reservation> next = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                    .eq(Reservation::getBookId, r.getBookId())
                    .eq(Reservation::getStatus, "WAITING")
                    .orderByAsc(Reservation::getQueuePosition));
            if (!next.isEmpty()) {
                Reservation n = next.get(0);
                n.setStatus("FULFILLED");
                n.setPickupDeadline(LocalDateTime.now().plusDays(3));
                reservationMapper.updateById(n);
                notificationService.create(n.getUserId(), "RESERVE_READY", "预约书已到馆", "请于3天内到馆借阅", n.getId());
                // 后续等待者排队位置前移一位
                for (int i = 1; i < next.size(); i++) {
                    Reservation after = next.get(i);
                    after.setQueuePosition(after.getQueuePosition() - 1);
                    reservationMapper.updateById(after);
                }
            } else {
                Book book = bookMapper.selectById(r.getBookId());
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                bookMapper.updateById(book);
            }
        }
        log.info("ReservationExpireTask done");
    }

    /**
     * 将指定图书中排队位置大于 cancelledPosition 的等待者前移一位
     */
    private void shiftQueuePosition(Long bookId, Integer cancelledPosition) {
        reservationMapper.selectList(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getBookId, bookId)
                .eq(Reservation::getStatus, "WAITING")
                .gt(Reservation::getQueuePosition, cancelledPosition)
                .orderByAsc(Reservation::getQueuePosition))
            .forEach(next -> {
                next.setQueuePosition(next.getQueuePosition() - 1);
                reservationMapper.updateById(next);
            });
    }
}
