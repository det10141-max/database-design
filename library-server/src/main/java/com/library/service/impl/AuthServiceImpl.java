package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.BusinessException;
import com.library.config.AppProperties;
import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.entity.RefreshToken;
import com.library.entity.TokenBlacklist;
import com.library.entity.User;
import com.library.mapper.RefreshTokenMapper;
import com.library.mapper.TokenBlacklistMapper;
import com.library.mapper.UserMapper;
import com.library.security.JwtTokenProvider;
import com.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final TokenBlacklistMapper tokenBlacklistMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, req.getUsername())
                        .eq(User::getIsDeleted, 0));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken();

        // 保存 RefreshToken
        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setToken(refreshTokenStr);
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000));
        refreshTokenMapper.insert(rt);

        return new LoginResponse(accessToken, refreshTokenStr,
                user.getId(), user.getUsername(), user.getRole(), user.getRealName());
    }

    @Override
    @Transactional
    public void register(RegisterRequest req) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRealName(req.getRealName());
        user.setRole("READER");
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        userMapper.insert(user);
    }

    @Override
    public LoginResponse refresh(String refreshTokenStr) {
        RefreshToken rt = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getToken, refreshTokenStr));
        if (rt == null || rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(401, "RefreshToken无效或已过期");
        }
        User user = userMapper.selectById(rt.getUserId());
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(401, "用户状态异常");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken();

        // 替换 RefreshToken
        refreshTokenMapper.deleteById(rt.getId());
        RefreshToken newRt = new RefreshToken();
        newRt.setUserId(user.getId());
        newRt.setToken(newRefreshToken);
        newRt.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000));
        refreshTokenMapper.insert(newRt);

        return new LoginResponse(newAccessToken, newRefreshToken,
                user.getId(), user.getUsername(), user.getRole(), user.getRealName());
    }

    @Override
    public void logout(String jti, long expiresAtMillis) {
        TokenBlacklist tb = new TokenBlacklist();
        tb.setJti(jti);
        tb.setExpiresAt(LocalDateTime.now().plusSeconds(expiresAtMillis / 1000));
        tokenBlacklistMapper.insert(tb);
    }
}
