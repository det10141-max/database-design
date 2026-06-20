package com.library.controller.reader;
import com.library.common.Result;
import com.library.security.LoginUser;
import com.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reader/reservations")
@RequiredArgsConstructor
public class ReaderReserveController {
    private final ReservationService reservationService;

    @PostMapping
    public Result<?> reserve(@AuthenticationPrincipal LoginUser user, @RequestBody Map<String,Long> body) {
        reservationService.reserve(user.getId(), body.get("bookId"));
        return Result.ok();
    }

    @GetMapping
    public Result<?> list(@AuthenticationPrincipal LoginUser user) {
        return Result.ok(reservationService.listByUser(user.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<?> cancel(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        reservationService.cancel(user.getId(), id);
        return Result.ok();
    }

    /** 预约到书后，用户取书借阅 */
    @PutMapping("/{id}/pickup")
    public Result<?> pickup(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        reservationService.fulfill(id);
        return Result.ok();
    }
}
