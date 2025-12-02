package com.tenxi.controller;

import com.tenxi.entity.es.CourseDocument;
import com.tenxi.entity.vo.CourseSimpleVO;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.CoursePublishDTO;

import com.tenxi.entity.vo.CourseVO;
import com.tenxi.handler.ControllerHandler;

import com.tenxi.service.CourseService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class  CourseController {
    @Resource
    private ControllerHandler controllerHandler;
    @Resource
    private CourseService courseService;


    @Operation(
            summary = "发布课程",
            description = "教师上传课程",
            responses = {
                    @ApiResponse(responseCode = "2004", description = "课程发布失败")
            }
    )
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/publish")
    public RestBean<String> publishCourse(@ModelAttribute CoursePublishDTO dto) {
        return courseService.publishCourse(dto);
    }


    @Operation(
            summary = "删除课程",
            description = "教师删除课程",
            responses = {
                    @ApiResponse(responseCode = "2002", description = "课程删除失败")
            }
    )
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @DeleteMapping("/id")
    public RestBean<String> deleteCourse(@RequestParam("id") Long id) {
        return courseService.deleteCourseById(id);
    }

    @Operation(
            summary = "更新课程",
            description = "教师更新课程",
            responses = {
                    @ApiResponse(responseCode = "2005", description = "课程更新失败")
            }
    )
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/update/{id}")
    public RestBean<String> updateCourse(@ModelAttribute CoursePublishDTO dto, @PathVariable("id") Long id) {
        return courseService.updateCourse(dto, id);
    }


    @Operation(
            summary = "根据用户的描述查询相关的课程",
            description = "根据用户在搜索框中输入或者点击的分类查询相关的课程"
    )
    @GetMapping("/search/{des}")
    public RestBean<List<CourseDocument>> queryCourse(@PathVariable("des") String des) {
        return courseService.queryCourse(des);
    }



    @Operation(
            summary = "根据id查询课程的详细信息"
    )
    @GetMapping("/{id}")
    public RestBean<CourseVO> queryCourse(@PathVariable("id") Long id) {
        return courseService.getCourseById(id);
    }



    @Hidden
    @GetMapping("/simple/{id}")
    public CourseSimpleVO getSimpleCourse(@PathVariable("id") Long id) {
        return courseService.getSimpleCourseById(id);
    }

    /**
     * 根据课程的id查询订阅了该课程的用户id
     * @param id
     * @return
     */
    @Hidden
    @GetMapping("/subscribe/{id}")
    public List<Long> getCourseSubscribers(@PathVariable("id") Long id) {
        return courseService.getCourseSubscribers(id);
    }
}
