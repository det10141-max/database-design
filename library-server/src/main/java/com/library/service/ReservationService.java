package com.library.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Reservation;
import java.util.List;
public interface ReservationService {
    void reserve(Long userId, Long bookId);
    void cancel(Long userId, Long reservationId);
    void fulfill(Long reservationId);
    List<Reservation> listByUser(Long userId);
    /** 管理员分页查询所有预约（含书名、用户名） */
    Page<Reservation> pageAll(int page, int pageSize, String status);
    /** 管理员取消预约（支持 WAITING / FULFILLED，自动处理库存与排队位置） */
    void adminCancel(Long reservationId);
}