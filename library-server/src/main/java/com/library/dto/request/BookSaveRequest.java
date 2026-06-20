package com.library.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class BookSaveRequest {
    @NotBlank private String isbn;
    @NotBlank private String title;
    @NotBlank private String author;
    private String publisher;
    private Long categoryId;
    private Integer publishYear;
    private String edition;
    private BigDecimal price;
    @NotNull private Integer totalCopies;
    private String location;
    private String coverUrl;
    private String description;
}