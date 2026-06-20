package com.library.controller.admin;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.request.FineRequest;
import com.library.service.FineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/fines")
@RequiredArgsConstructor
public class AdminFineController {
    private final FineService fineService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int pageSize) {
        var p = fineService.pageAll(page, pageSize);
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody FineRequest req) {
        fineService.create(req);
        return Result.ok();
    }

    @PutMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id) {
        fineService.pay(id);
        return Result.ok();
    }
}
