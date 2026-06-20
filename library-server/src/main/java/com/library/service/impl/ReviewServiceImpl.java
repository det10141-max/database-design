package com.library.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.BusinessException;
import com.library.dto.request.ReviewRequest;
import com.library.entity.BookReview;
import com.library.entity.BorrowRecord;
import com.library.mapper.BookReviewMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final BookReviewMapper reviewMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    @Override
    public void create(Long userId, ReviewRequest req) {
        // 校验是否借过并已归还
        Long cnt = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId,userId).eq(BorrowRecord::getBookId,req.getBookId()).eq(BorrowRecord::getStatus,"RETURNED"));
        if (cnt == 0) throw new BusinessException("您尚未借阅该书，无法评论");
        if (reviewMapper.selectCount(new LambdaQueryWrapper<BookReview>().eq(BookReview::getUserId,userId).eq(BookReview::getBookId,req.getBookId())) > 0)
            throw new BusinessException("您已评论过该书");
        BookReview r = new BookReview();
        r.setUserId(userId);
        r.setBookId(req.getBookId());
        r.setRating(req.getRating());
        r.setContent(req.getContent());
        reviewMapper.insert(r);
    }

    @Override
    public List<BookReview> listByBook(Long bookId) {
        return reviewMapper.selectList(new LambdaQueryWrapper<BookReview>().eq(BookReview::getBookId,bookId).orderByDesc(BookReview::getCreatedAt));
    }
}
