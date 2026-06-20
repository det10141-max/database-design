package com.library.controller.reader;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.security.LoginUser;
import com.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reader/borrows")
@RequiredArgsConstructor
public class ReaderBorrowController {
    private final BorrowService borrowService;

    @PostMapping
    public Result<?> borrow(@AuthenticationPrincipal LoginUser user, @RequestBody java.util.Map<String,Long> body) {
        borrowService.borrow(user.getId(), body.get("bookId"));
        return Result.ok();
    }

    @GetMapping("/current")
    public Result<?> current(@AuthenticationPrincipal LoginUser user,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int pageSize) {
        var p = borrowService.pageByUser(user.getId(), page, pageSize, "BORROWING");
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/history")
    public Result<?> history(@AuthenticationPrincipal LoginUser user,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int pageSize) {
        var p = borrowService.pageByUser(user.getId(), page, pageSize, null);
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @PutMapping("/{id}/renew")
    public Result<?> renew(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        borrowService.renew(user.getId(), id);
        return Result.ok();
    }

    @PutMapping("/{id}/return")
    public Result<?> returnBook(@PathVariable Long id) {
        borrowService.returnBook(id);
        return Result.ok();
    }

    @GetMapping("/eligibility")
    public Result<?> eligibility(@AuthenticationPrincipal LoginUser user) {
        String reason = borrowService.checkEligibility(user.getId());
        if (reason.isEmpty()) return Result.ok(java.util.Map.of("can", true));
        return Result.ok(java.util.Map.of("can", false, "reason", reason));
    }
}
