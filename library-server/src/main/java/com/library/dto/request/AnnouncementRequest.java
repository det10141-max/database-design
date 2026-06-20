package com.library.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class AnnouncementRequest {
    @NotBlank private String title;
    private String content;
    private Integer isPinned;
    private Integer status;
    private LocalDateTime publishAt;
}