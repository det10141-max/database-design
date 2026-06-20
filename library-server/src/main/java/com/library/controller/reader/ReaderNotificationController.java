package com.library.controller.reader;
import com.library.common.Result;
import com.library.security.LoginUser;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reader/notifications")
@RequiredArgsConstructor
public class ReaderNotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public Result<?> list(@AuthenticationPrincipal LoginUser user) {
        return Result.ok(notificationService.listByUser(user.getId()));
    }

    @PutMapping("/{id}/read")
    public Result<?> markRead(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        notificationService.markRead(user.getId(), id);
        return Result.ok();
    }
}
