package com.tenxi.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Collect {
    private Long id;
    private Long userId;
    private Long courseId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime collectTime;
}
