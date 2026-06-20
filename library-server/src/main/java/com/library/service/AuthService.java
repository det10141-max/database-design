package com.library.service;
import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
public interface AuthService {
    LoginResponse login(LoginRequest req);
    void register(RegisterRequest req);
    LoginResponse refresh(String refreshToken);
    void logout(String jti, long expiresAt);
}