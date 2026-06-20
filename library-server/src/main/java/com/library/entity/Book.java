package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图书表
 */
@Data
@TableName("books")
public class Book {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Long categoryId;
    private Integer publishYear;
    private String edition;
    private BigDecimal price;
    private Integer totalCopies;
    private Integer availableCopies;
    private String location;
    private String coverUrl;
    private String description;
    private Integer status;     // 1=在架 0=下架

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
