package com.tenxi.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetDto {
    private String email;
    private String code;
    private String oldPassword;
    private String newPassword;
}
