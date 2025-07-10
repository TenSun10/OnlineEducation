package com.tenxi.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("category")
public class Category {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String label;
    private Long parentId;
    private Integer level;
    private Integer orderNum;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Category(String label, Long parentId, Integer level, Integer orderNum, Integer status, LocalDateTime createTime) {
        this.label = label;
        this.parentId = parentId;
        this.level = level;
        this.orderNum = orderNum;
        this.status = status;
        this.createTime = createTime;
    }
}
