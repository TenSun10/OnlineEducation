package com.tenxi.controller;

import com.tenxi.entity.vo.CourseSimpleVO;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.CoursePublishDTO;

import com.tenxi.entity.vo.CourseVO;
import com.tenxi.handler.ControllerHandler;

import com.tenxi.service.CourseService;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 主要用于课程视频发布和查询的控制层
 */
@Log
@RestController
@RequestMapping("/course")
public class CourseController {
    @Resource
    private ControllerHandler controllerHandler;
    @Resource
    private CourseService courseService;

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/publish")
    public RestBean<String> publishCourse(@RequestBody CoursePublishDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("用户权限: " + auth.getAuthorities());
        return controllerHandler.messageHandler(() ->
                courseService.publishCourse(dto));
    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @DeleteMapping("/id")
    public RestBean<String> deleteCourse(@RequestParam("id") Integer id) {
        return controllerHandler.messageHandler(() ->
                courseService.deleteCourseById(id));
    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/update/{id}")
    public RestBean<String> updateCourse(@RequestBody CoursePublishDTO dto, @PathVariable Long id) {
        return controllerHandler.messageHandler(() ->
                courseService.updateCourse(dto, id));
    }


    @GetMapping("/search/{des}")
    public RestBean<List<CourseVO>> queryCourse(@PathVariable String des) {
        return courseService.queryCourse(des);
    }

    @GetMapping("/collect/{id}")
    public RestBean<String> collectCourse(@PathVariable Long id) {
        return controllerHandler.messageHandler(() ->
                courseService.collectCourse(id));
    }

    @GetMapping("/{id}")
    public RestBean<CourseVO> queryCourse(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @GetMapping("/simple/{id}")
    public CourseSimpleVO getSimpleCourse(@PathVariable Long id) {
        return courseService.getSimpleCourseById(id);
    }
}
