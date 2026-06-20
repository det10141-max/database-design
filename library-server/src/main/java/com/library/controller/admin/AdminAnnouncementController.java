package com.library.controller.admin;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.request.AnnouncementRequest;
import com.library.security.LoginUser;
import com.library.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {
    private final AnnouncementService announcementService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int pageSize) {
        var p = announcementService.page(page, pageSize);
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody AnnouncementRequest req, @AuthenticationPrincipal LoginUser user) {
        announcementService.create(user.getId(), req);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest req) {
        announcementService.update(id, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.ok();
    }
}
