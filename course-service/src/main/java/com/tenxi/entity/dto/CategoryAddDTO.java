package com.tenxi.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryAddDTO {
    private String label;
    private Long parentId;
    private Integer level;
    private Integer orderNum;
    private Integer status;
}
