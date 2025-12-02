package com.tenxi.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseSimpleVO {
    private Long id;
    private Float originPrice;
    private Float discountPrice;
    private String title;
    private String introduction;
    private Long pusherId;
    private String pusherName;
}
