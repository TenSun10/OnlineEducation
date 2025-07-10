package com.tenxi.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentAddDTO {
    private Long courseId;
    @JsonProperty(required = false)
    private Long parentId;
    private String content;
}
