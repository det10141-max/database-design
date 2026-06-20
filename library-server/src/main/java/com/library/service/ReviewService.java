package com.library.service;
import com.library.dto.request.ReviewRequest;
import com.library.entity.BookReview;
import java.util.List;
public interface ReviewService {
    void create(Long userId, ReviewRequest req);
    List<BookReview> listByBook(Long bookId);
}