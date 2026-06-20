package com.library.controller.admin;

import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Reservation;
import com.library.mapper.ReservationMapper;
import com.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员预约管理接口
 */
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    /** 分页查询所有预约（可按状态筛选） */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int pageSize,
                          @RequestParam(required = false) String status) {
        var p = reservationService.pageAll(page, pageSize, status);
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    /** 管理员取消预约（复用 cancel 逻辑，自动更新排队位置） */
    @DeleteMapping("/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null) throw new BusinessException("预约记录不存在");
        reservationService.cancel(r.getUserId(), id);
        return Result.ok();
    }
}
