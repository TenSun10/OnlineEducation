package com.tenxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenxi.entity.po.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT id from course")
    List<Long> selectAllCourseIds();
}
