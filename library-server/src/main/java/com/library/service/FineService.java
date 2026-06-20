package com.library.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.request.FineRequest;
import com.library.entity.Fine;
public interface FineService {
    void create(FineRequest req);
    void pay(Long fineId);
    Page<Fine> pageAll(int page, int pageSize);
    Page<Fine> pageByUser(Long userId, int page, int pageSize);
}