package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 借阅记录表
 */
@Data
@TableName("borrow_records")
public class BorrowRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long bookId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Integer renewCount;
    private String status;  // BORROWING / RETURNED / LOST

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
