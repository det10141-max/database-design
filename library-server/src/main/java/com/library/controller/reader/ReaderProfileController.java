package com.library.controller.reader;
import com.library.common.Result;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reader/profile")
@RequiredArgsConstructor
public class ReaderProfileController {
    private final UserMapper userMapper;

    @GetMapping
    public Result<?> profile(@AuthenticationPrincipal LoginUser user) {
        User u = userMapper.selectById(user.getId());
        if (u != null) u.setPassword(null);
        return Result.ok(u);
    }
}
