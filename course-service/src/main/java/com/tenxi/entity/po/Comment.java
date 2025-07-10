package com.tenxi.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    private Long id;
    private Long courseId;
    private Long userId;
    private String content;
    private Long parentId;
    private LocalDateTime createTime;
}
