package com.library.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.dto.request.FineRequest;
import com.library.entity.Fine;
import com.library.mapper.FineMapper;
import com.library.service.FineService;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {
    private final FineMapper fineMapper;
    private final NotificationService notificationService;

    @Override
    public void create(FineRequest req) {
        Fine f = new Fine();
        f.setUserId(req.getUserId());
        f.setBorrowRecordId(req.getBorrowRecordId());
        f.setAmount(req.getAmount());
        f.setReason(req.getReason());
        fineMapper.insert(f);
        notificationService.create(req.getUserId(),"FINE","罚款通知","您有新的罚款: "+req.getAmount()+"元",f.getId());
    }

    @Override
    @Transactional
    public void pay(Long fineId) {
        // 行级锁查询，防止并发下多次缴费
        Fine f = fineMapper.selectByIdForUpdate(fineId);
        if (f == null) throw new BusinessException("罚款记录不存在");
        if ("PAID".equals(f.getStatus())) throw new BusinessException("该罚款已缴纳，无需重复操作");
        f.setStatus("PAID");
        f.setPaidAt(LocalDateTime.now());
        fineMapper.updateById(f);
    }

    @Override
    public Page<Fine> pageAll(int page, int pageSize) {
        return fineMapper.selectPage(new Page<>(page,pageSize), new LambdaQueryWrapper<Fine>().orderByDesc(Fine::getCreatedAt));
    }

    @Override
    public Page<Fine> pageByUser(Long userId, int page, int pageSize) {
        return fineMapper.selectPage(new Page<>(page,pageSize), new LambdaQueryWrapper<Fine>().eq(Fine::getUserId,userId).orderByDesc(Fine::getCreatedAt));
    }
}
