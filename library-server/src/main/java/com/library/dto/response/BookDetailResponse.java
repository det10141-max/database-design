package com.library.dto.response;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class BookDetailResponse {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String categoryName;
    private Integer publishYear;
    private String edition;
    private BigDecimal price;
    private Integer totalCopies;
    private Integer availableCopies;
    private String location;
    private String coverUrl;
    private String description;
    private Integer status;
    private String stockStatus; // AVAILABLE / RESERVED / UNAVAILABLE
    private Boolean hasBorrowed; // 当前用户是否借阅过该书（用于评论权限控制）
    private Double avgRating;
    private Integer reviewCount;
    private List<ReviewItem> reviews;
    @Data
    public static class ReviewItem {
        private Long id;
        private String username;
        private Integer rating;
        private String content;
        private LocalDateTime createdAt;
    }
}