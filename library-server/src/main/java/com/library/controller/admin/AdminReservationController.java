package com.library.controller.admin;

import com.library.common.PageResult;
import com.library.common.Result;
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

    /** 分页查询所有预约（可按状态筛选） */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int pageSize,
                          @RequestParam(required = false) String status) {
        var p = reservationService.pageAll(page, pageSize, status);
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    /** 管理员取消预约（支持 WAITING / FULFILLED，自动处理库存与排队位置） */
    @DeleteMapping("/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        reservationService.adminCancel(id);
        return Result.ok();
    }
}
