package com.tenxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenxi.entity.po.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
