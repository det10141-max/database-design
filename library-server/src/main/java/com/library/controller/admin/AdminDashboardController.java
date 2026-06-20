package com.library.controller.admin;
import com.library.common.Result;
import com.library.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public Result<?> stats() { return Result.ok(dashboardService.stats()); }
}
