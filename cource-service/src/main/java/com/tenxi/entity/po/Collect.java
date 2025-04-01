package com.tenxi.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Collect {
    private Long id;
    private Long userId;
    private Long courseId;
}
