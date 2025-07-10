package com.tenxi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagVO {
    private Long id;
    private String name;
    private LocalDateTime createTime;
}
