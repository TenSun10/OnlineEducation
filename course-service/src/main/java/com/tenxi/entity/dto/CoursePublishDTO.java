package com.tenxi.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePublishDTO {
    private Long categoryId;
    private String title;
    private String introduction;
    private Float originPrice;
    private Float discountPrice;



    /*
    处理多个tag以及tag自由创建问题:
    选择用字符串存储所有的tag,格式为#tag1#tag2...
    然后再利用#来分割,依次查询是否存在
    1.存在:直接在tag_course表中存储
    2.不存在:先在tag表中存储,再在tag_course表中存储
     */
    private String tags;

    @Schema(description = "课程视频文件")
    private MultipartFile videoFile;

    @Schema(description = "封面图片文件")
    private MultipartFile imageFile;

    // 文件更新标识
    @Schema(description = "是否更新视频文件")
    private Boolean updateVideo = false;

    @Schema(description = "是否更新封面图片")
    private Boolean updateCover = false;
}
