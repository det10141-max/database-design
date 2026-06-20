package com.library.dto.response;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 图书评分视图 VO — 映射 v_book_rating
 */
@Data
public class BookRatingVO {
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private Long reviewCount;
    private BigDecimal avgRating;
    private Long fiveStar;
    private Long fourStar;
    private Long lowStar;
}
