package com.tenxi.progress.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoProgressVO {
    private Long id;
    private Long videoId;
    private Long userId;
    private Integer progressSecond;
    private Integer totalSecond;
    private BigDecimal percentage;
}
