package com.library.controller.reader;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.request.BookQueryRequest;
import com.library.security.LoginUser;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reader/books")
@RequiredArgsConstructor
public class ReaderBookController {
    private final BookService bookService;

    @GetMapping
    public Result<?> list(BookQueryRequest req) {
        var page = bookService.page(req);
        return Result.ok(new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @GetMapping("/popular")
    public Result<?> popular(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(bookService.popular(limit));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        // 传入当前用户 ID，用于查询是否借阅过该书（评论权限控制）
        Long userId = user != null ? user.getId() : null;
        return Result.ok(bookService.detail(id, userId));
    }
}
