package com.tenxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenxi.entity.po.CourseSubscribe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseSubscribeMapper extends BaseMapper<CourseSubscribe> {
    @Select("SELECT account_id from course_subscribe where course_id = #{courseId}")
    List<Long> getCourseSubscribers(@Param("courseId") Long courseId);
}
