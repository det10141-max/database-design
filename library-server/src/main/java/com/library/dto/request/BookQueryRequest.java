package com.library.dto.request;
import lombok.Data;
@Data
public class BookQueryRequest {
    private String keyword;
    private Long categoryId;
    private Integer page = 1;
    private Integer pageSize = 10;
}