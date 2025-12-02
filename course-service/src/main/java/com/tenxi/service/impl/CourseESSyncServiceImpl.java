package com.tenxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tenxi.client.AccountClient;
import com.tenxi.entity.es.CourseDocument;
import com.tenxi.entity.es.TagDocument;
import com.tenxi.entity.po.Course;
import com.tenxi.entity.po.Tag;
import com.tenxi.mapper.CategoryMapper;
import com.tenxi.mapper.CourseMapper;
import com.tenxi.mapper.TagMapper;
import com.tenxi.service.CourseESSyncService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseESSyncServiceImpl implements CourseESSyncService {
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private ElasticsearchOperations elasticsearchOperations;
    @Resource
    private AccountClient accountClient;

    /**
     * 全量更新某课程
     * @param courseId
     */
    @Override
    public void fullSyncCourseToES(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            //课程不存在,从ES中删除
            elasticsearchOperations.delete(courseId.toString(), CourseDocument.class);
            return;
        }

        CourseDocument courseDocument = convertToDocument(course);
        elasticsearchOperations.save(courseDocument); //会自动更新已存在的文档

    }

    /**
     * 全量更新批量课程
     * @param courseIds
     */
    @Override
    public void batchFullSyncCourseToES(List<Long> courseIds) {
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        List<CourseDocument> documents = courses.stream()
                .map(this::convertToDocument)
                .toList();

        elasticsearchOperations.save(documents);
    }

    /**
     * 部分更新
     * @param courseId
     * @param updateField
     */
    @Override
    public void partialSyncCourseToES(Long courseId, Map<String, Object> updateField) {
        UpdateQuery updateQuery = UpdateQuery.builder(courseId.toString())
                .withDocument(Document.from(updateField))
                .build();

        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("course"));
    }

    @Override
    public void deleteCourseFromES(Long courseId) {
        elasticsearchOperations.delete(courseId.toString(), CourseDocument.class);
    }

    private CourseDocument convertToDocument(Course course) {
        CourseDocument document = new CourseDocument();
        BeanUtils.copyProperties(course, document);

        String pusherName = accountClient.queryAccountById(course.getPusherId()).data().getUsername();
        document.setPusherName(pusherName);
        String categoryName = categoryMapper.selectById(course.getCategoryId()).getLabel();
        document.setCategoryName(categoryName);
        document.setTags(getTagDocuments(course.getId()));
        document.setSearchBoost(calculateSearchBoost(course));

        return document;
    }

    //获取课程相关的标签
    private List<TagDocument> getTagDocuments(Long courseId) {
        // 查询课程的标签
        QueryWrapper<Tag> tagWrapper = new QueryWrapper<>();
        tagWrapper.inSql("id", "SELECT tag_id FROM course_tag WHERE course_id = " + courseId);
        List<Tag> tags = tagMapper.selectList(tagWrapper);
        return tags.stream()
                .map(tag -> new TagDocument(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

    private Integer calculateSearchBoost(Course course) {
        //根据一定的规则设置不同课程在搜索时的权重
        int boost = 0;
        boost += course.getEnrolledCount() != null ? course.getEnrolledCount() : 0;
        boost += course.getRating() != null ? (int) (course.getRating() * 10) : 0;
        boost += course.getCommentCount() != null ? course.getCommentCount() : 0;
        return boost;
    }
}
