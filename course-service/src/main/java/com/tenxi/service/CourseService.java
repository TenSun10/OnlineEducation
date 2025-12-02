package com.tenxi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.entity.es.CourseDocument;
import com.tenxi.entity.vo.CourseSimpleVO;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.CoursePublishDTO;
import com.tenxi.entity.po.Course;
import com.tenxi.entity.vo.CourseVO;

import java.util.List;

public interface CourseService extends IService<Course> {
    RestBean<String> publishCourse(CoursePublishDTO dto);

    RestBean<List<CourseDocument>> queryCourse(String des);

    RestBean<List<CourseDocument>> getByTag(Long id);

    RestBean<CourseVO> getCourseById(Long id);

    CourseSimpleVO getSimpleCourseById(Long id);

    RestBean<String> deleteCourseById(Long id);

    RestBean<String> updateCourse(CoursePublishDTO dto, Long id);

    List<Long> getCourseSubscribers(Long id);

    List<CourseVO> getBatchCourses(List<Long> courseIds);


}
