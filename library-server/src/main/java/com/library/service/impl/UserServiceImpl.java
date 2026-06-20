package com.library.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    public Page<User> page(int page, int pageSize, String keyword) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>().eq(User::getIsDeleted,0);
        if (keyword != null && !keyword.isEmpty()) qw.and(w -> w.like(User::getUsername,keyword).or().like(User::getRealName,keyword));
        qw.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(new Page<>(page,pageSize), qw);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        User u = userMapper.selectById(id);
        if (u == null) throw new BusinessException("用户不存在");
        u.setStatus(status);
        userMapper.updateById(u);
    }
}
