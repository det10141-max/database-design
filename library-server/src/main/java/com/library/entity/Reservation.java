package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约表
 */
@Data
@TableName("reservations")
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long bookId;
    private LocalDateTime reserveDate;
    private Integer queuePosition;
    private LocalDateTime expireDate;
    private LocalDateTime pickupDeadline;
    private String status;  // WAITING / FULFILLED / CANCELLED / EXPIRED

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 非数据库字段：关联的书名（用于前端展示） */
    @TableField(exist = false)
    private String bookTitle;

    /** 非数据库字段：关联的用户名（用于管理员展示） */
    @TableField(exist = false)
    private String username;
}
