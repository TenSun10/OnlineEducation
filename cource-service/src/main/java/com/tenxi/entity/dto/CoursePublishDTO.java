package com.tenxi.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePublishDTO {
    private Long categoryId;
    private String title;
    private String introduction;
    private String url;
    private Float originPrice;
    private Float discountPrice;
    private String coverImage;



    /*
    处理多个tag以及tag自由创建问题:
    选择用字符串存储所有的tag,格式为#tag1#tag2...
    然后再利用#来分割,依次查询是否存在
    1.存在:直接在tag_course表中存储
    2.不存在:先在tag表中存储,再在tag_course表中存储
     */
    private String tags;
}
