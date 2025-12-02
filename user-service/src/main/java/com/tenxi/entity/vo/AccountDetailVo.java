package com.tenxi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetailVo {
    private Long id;
    private String email;
    private String username;
    private String avatar;
    private LocalDateTime registerTime;

}
