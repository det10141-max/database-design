package com.library.controller.reader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Fine;
import com.library.mapper.FineMapper;
import com.library.security.LoginUser;
import com.library.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reader/fines")
@RequiredArgsConstructor
public class ReaderFineController {
    private final FineService fineService;
    private final FineMapper fineMapper;

    @GetMapping
    public Result<?> list(@AuthenticationPrincipal LoginUser user,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int pageSize) {
        var p = fineService.pageByUser(user.getId(), page, pageSize);
        return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    /** 缴纳罚款（模拟）：校验罚款归属后标记为已缴 */
    @PutMapping("/{id}/pay")
    public Result<?> pay(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        // 校验该罚款属于当前用户
        Fine fine = fineMapper.selectOne(new LambdaQueryWrapper<Fine>()
                .eq(Fine::getId, id)
                .eq(Fine::getUserId, user.getId()));
        if (fine == null) throw new BusinessException("罚款记录不存在");
        fineService.pay(id);
        return Result.ok();
    }
}
