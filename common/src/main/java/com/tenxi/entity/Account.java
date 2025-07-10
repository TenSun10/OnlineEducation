package com.tenxi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private Long id;
    private String username;
    private String email;
    private String password;
    private String avatar;
    private String role;

    @JsonIgnore
    private LocalDateTime registerTime;

    public Account(String email, String encode, String role, LocalDateTime now) {
        this.email = email;
        this.password = encode;
        this.role = role;
        registerTime = now;
    }
}