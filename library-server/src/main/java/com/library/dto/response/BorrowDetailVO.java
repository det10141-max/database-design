package com.library.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 借阅详情视图 VO — 映射 v_borrow_detail
 */
@Data
public class BorrowDetailVO {
    private Long id;
    private Long userId;
    private String userName;
    private Long bookId;
    private String bookTitle;
    private String author;
    private String isbn;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Integer renewCount;
    private String status;
    private Long daysRemaining;
}
