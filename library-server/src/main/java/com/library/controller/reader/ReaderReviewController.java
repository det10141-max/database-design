package com.library.controller.reader;
import com.library.common.Result;
import com.library.dto.request.ReviewRequest;
import com.library.security.LoginUser;
import com.library.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reader/reviews")
@RequiredArgsConstructor
public class ReaderReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Result<?> create(@AuthenticationPrincipal LoginUser user, @Valid @RequestBody ReviewRequest req) {
        reviewService.create(user.getId(), req);
        return Result.ok();
    }
}
