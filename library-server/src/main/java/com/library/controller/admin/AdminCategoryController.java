package com.library.controller.admin;
import com.library.common.Result;
import com.library.dto.request.CategorySaveRequest;
import com.library.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Result<?> list() { return Result.ok(categoryService.tree()); }

    @PostMapping
    public Result<?> create(@Valid @RequestBody CategorySaveRequest req) {
        categoryService.save(req);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody CategorySaveRequest req) {
        categoryService.update(id, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
