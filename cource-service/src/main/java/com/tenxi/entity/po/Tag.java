package com.tenxi.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 给课程添加标签的操作:
 * 1.根据用户的输入在数据库中搜索对用的标签
 * 2.数据库中没有这种标签则创建标签
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    @TableId(type= IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private LocalDateTime createTime;

    public Tag(String tagName, LocalDateTime now) {
        this.name = tagName;
        this.createTime = now;
    }
}
