package com.library.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class RegisterRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String realName;
    private String phone;
    private String email;
}