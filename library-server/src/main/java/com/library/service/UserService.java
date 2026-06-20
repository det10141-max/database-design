package com.library.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.User;
public interface UserService {
    Page<User> page(int page, int pageSize, String keyword);
    void updateStatus(Long id, Integer status);
}