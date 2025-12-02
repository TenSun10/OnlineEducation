package com.tenxi.service;

import java.util.List;
import java.util.Map;

/**
 * 课程信息ES和数据库同步
 */
public interface CourseESSyncService {
    //将某个课程信息全部同步到ES
    void fullSyncCourseToES(Long courseId);

    //批量同步课程全部信息
    void batchFullSyncCourseToES(List<Long> courseIds);

    //更新课程部分字段到ES
    void partialSyncCourseToES(Long courseId, Map<String, Object> updateField);

    void deleteCourseFromES(Long courseId);
}
