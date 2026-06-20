package com.library.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.response.OverdueBookVO;
import com.library.mapper.OverdueMapper;
import com.library.service.BorrowService;
import com.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/borrows")
@RequiredArgsConstructor
public class AdminBorrowController {
    private final BorrowService borrowService;
    private final ReservationService reservationService;
    private final OverdueMapper overdueMapper;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int pageSize,
                          @RequestParam(required = false) String status) {
        var p = borrowService.pageAll(page, pageSize, status);
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @PostMapping("/{id}/return")
    public Result<?> returnBook(@PathVariable Long id) {
        borrowService.returnBook(id);
        return Result.ok();
    }

    @PutMapping("/{id}/lost")
    public Result<?> markLost(@PathVariable Long id) {
        borrowService.markLost(id);
        return Result.ok();
    }

    @GetMapping("/overdue")
    public Result<?> overdue(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int pageSize) {
        var qw = new LambdaQueryWrapper<OverdueBookVO>().orderByDesc(OverdueBookVO::getOverdueDays);
        var p = overdueMapper.selectPageView(new Page<>(page, pageSize), qw);
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @PutMapping("/reservations/{id}/fulfill")
    public Result<?> fulfill(@PathVariable Long id) {
        reservationService.fulfill(id);
        return Result.ok();
    }
}
