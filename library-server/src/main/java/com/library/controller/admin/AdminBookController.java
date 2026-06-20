package com.library.controller.admin;
import com.library.common.Result;
import com.library.dto.request.BookQueryRequest;
import com.library.dto.request.BookSaveRequest;
import com.library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.library.common.PageResult;

@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
public class AdminBookController {
    private final BookService bookService;

    @GetMapping
    public Result<?> list(BookQueryRequest req) {
        var page = bookService.page(req);
        return Result.ok(new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(bookService.detail(id, null));
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody BookSaveRequest req) {
        bookService.save(req);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody BookSaveRequest req) {
        bookService.update(id, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.ok();
    }
}
