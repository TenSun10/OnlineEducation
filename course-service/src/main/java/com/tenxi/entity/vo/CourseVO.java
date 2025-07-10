package com.tenxi.entity.vo;

import com.tenxi.entity.po.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseVO {
    private Long id;
    private String title;
    private String introduction;
    private String url;
    private Long pusherId;
    private String pusherName;
    private Float originPrice;
    private Float discountPrice;
    private LocalDateTime updateAt;
    private float rating;
    private Integer commentCount;
    private Integer enrolledCount;
    private Integer isCollect;
    private List<Tag> tags;
}
