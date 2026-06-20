package com.library.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class CategorySaveRequest {
    @NotBlank private String name;
    private Long parentId;
    private Integer sortOrder;
}