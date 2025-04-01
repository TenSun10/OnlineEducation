package com.tenxi.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRegisterDto {
    private String email;
    private String password;
    private String role;
    private String code;
}
