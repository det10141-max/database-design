package com.library.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 借阅记录视图对象 — 含关联书名
 */
@Data
public class BorrowRecordVO {
    private Long id;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Integer renewCount;
    private String status;
    private LocalDateTime createdAt;
}
