package com.library.controller.common;
import com.library.common.Result;
import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.security.JwtTokenProvider;
import com.library.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return Result.ok();
    }

    @PostMapping("/refresh")
    public Result<?> refresh(@RequestBody java.util.Map<String,String> body) {
        return Result.ok(authService.refresh(body.get("refreshToken")));
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            String jti = jwtTokenProvider.getJti(token);
            authService.logout(jti, 1800000);
        }
        return Result.ok();
    }
}
