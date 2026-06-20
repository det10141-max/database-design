package com.library.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 逾期图书视图 VO — 映射 v_overdue_books
 */
@Data
public class OverdueBookVO {
    private Long id;
    private String userName;
    private String phone;
    private String bookTitle;
    private String author;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private Long overdueDays;
    private BigDecimal estimatedFine;
}
