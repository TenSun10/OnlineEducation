package com.tenxi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.entity.vo.CourseSimpleVO;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.CoursePublishDTO;
import com.tenxi.entity.po.Course;
import com.tenxi.entity.vo.CourseVO;

import java.util.List;

public interface CourseService extends IService<Course> {
    String publishCourse(CoursePublishDTO dto);

    RestBean<List<CourseVO>> queryCourse(String des);

    RestBean<List<CourseVO>> getByTag(Long id);

    RestBean<CourseVO> collectCourse(Long id);

    RestBean<CourseVO> getCourseById(Long id);

    CourseSimpleVO getSimpleCourseById(Long id);

    String deleteCourseById(Integer id);

    String updateCourse(CoursePublishDTO dto, Long id);
}
