package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.dto.response.BorrowRecordVO;

public interface BorrowService {
    void borrow(Long userId, Long bookId);
    void returnBook(Long borrowId);
    void renew(Long userId, Long borrowId);
    void markLost(Long borrowId);
    IPage<BorrowRecordVO> pageByUser(Long userId, int page, int pageSize, String status);
    IPage<BorrowRecordVO> pageAll(int page, int pageSize, String status);
    String checkEligibility(Long userId);
}