package com.library.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class FineRequest {
    @NotNull private Long userId;
    private Long borrowRecordId;
    @NotNull private BigDecimal amount;
    private String reason;
}