package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fines")
public class Fine {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long borrowRecordId;
    private BigDecimal amount;
    private String reason;
    private String status;  // UNPAID / PAID
    private LocalDateTime paidAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
