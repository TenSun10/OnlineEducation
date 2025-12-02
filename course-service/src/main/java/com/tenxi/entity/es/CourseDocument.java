package com.tenxi.entity.es;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "courses")
public class CourseDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private Long categoryId;

    @Field(type = FieldType.Text, analyzer = "ik_max_analyzer", searchAnalyzer = "ik_smart_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_analyzer", searchAnalyzer = "ik_smart_analyzer")
    private String introduction;

    @Field(type = FieldType.Keyword, index = false) //?index是干啥的
    private String videoUrl;

    @Field(type = FieldType.Keyword)
    private Long pusherId;

    @Field(type = FieldType.Text, analyzer = "ik_max_analyzer")
    private String pusherName;

    @Field(type = FieldType.Float)
    private Float discountPrice;

    @Field(type = FieldType.Float)
    private Float originPrice;

    @Field(type = FieldType.Keyword, index = false)
    private String coverImageUrl;

    @Field(type = FieldType.Half_Float)
    private Float rating;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Integer)
    private Integer enrolledCount;

    @Field(type = FieldType.Date, format = DateFormat.basic_date_time, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.basic_date_time, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Field(type = FieldType.Text, analyzer = "ik_max_analyzer", searchAnalyzer = "ik_smart_analyzer")
    private String categoryName;

    @Field(type = FieldType.Nested) //nested又是啥
    private List<TagDocument> tags;

    @Field(type = FieldType.Integer)
    private Integer searchBoost; //searchBoost被搜索的次数吗？


}
