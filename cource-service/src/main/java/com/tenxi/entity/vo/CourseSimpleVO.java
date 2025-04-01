package com.tenxi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSimpleVO {
    private Long id;
    private Float originPrice;
    private Float discountPrice;
    private String title;
    private String introduction;
    private Long pusherId;
    private String pusherName;
}
