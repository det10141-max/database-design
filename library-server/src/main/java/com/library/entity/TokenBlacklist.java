package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("token_blacklist")
public class TokenBlacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String jti;
    private LocalDateTime expiresAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
