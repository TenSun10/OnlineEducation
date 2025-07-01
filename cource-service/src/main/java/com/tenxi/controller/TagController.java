package com.tenxi.controller;

import com.tenxi.entity.po.Course;
import com.tenxi.entity.po.Tag;
import com.tenxi.entity.vo.CourseVO;
import com.tenxi.service.CourseService;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {
    @Resource
    private CourseService courseService;


    //根据tag的id查询相关的课程
    @GetMapping("/{id}")
    public RestBean<List<CourseVO>> getCoursesByTag(@PathVariable Long id) {
        return courseService.getByTag(id);
    }
}
