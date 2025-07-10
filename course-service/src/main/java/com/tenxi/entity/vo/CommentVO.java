package com.tenxi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {
    private Long id;
    private Long courseId;
    private Long commenterId;
    private String commenterUsername;
    private String content;
    private LocalDateTime createTime;
    private Long parentId;
    //展示回复评论使用的对象
    private List<CommentVO> comments;
}
