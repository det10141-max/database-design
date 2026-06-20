package com.library.controller.common;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {
    private final AnnouncementService announcementService;

    /** 公开公告列表（读者和管理员均可访问） */
    @GetMapping("/announcements")
    public Result<?> announcements(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "5") int pageSize) {
        var p = announcementService.page(page, pageSize);
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }
}
