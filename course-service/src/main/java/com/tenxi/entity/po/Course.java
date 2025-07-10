package com.tenxi.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long categoryId;
    private String title;
    private String introduction;
    private String url;
    private Long pusherId;
    private Float discountPrice;
    private Float originPrice;
    private String coverImage;
    private float rating;
    private Integer commentCount;
    private Integer enrolledCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //需要利用ThreadLocal实现,每一次更新自动填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
