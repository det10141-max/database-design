package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 热门图书排行项
 */
@Data
@AllArgsConstructor
public class PopularBookVO {
    private Long bookId;
    private String title;
    private String author;
    private Long borrowCount;
}
