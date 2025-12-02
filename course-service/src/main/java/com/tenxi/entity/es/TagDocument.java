package com.tenxi.entity.es;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagDocument {
    @Field(type = FieldType.Keyword)
    private Long tagId;

    @Field(type = FieldType.Text, analyzer = "ik_max_analyzer")
    private String tagName;
}
