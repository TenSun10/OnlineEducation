package com.tenxi.controller;

import com.tenxi.entity.vo.CourseVO;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.utils.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tenxi.service.CollectService;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/collect")
public class CollectController {
    @Resource
    private ControllerHandler controllerHandler;

    @Resource
    private CollectService collectService;


    @Operation(
            summary = "用户进行课程的收藏",
            description = "传入课程的id进行用户收藏操作"
    )
    @GetMapping("/{id}")
    public RestBean<String> userCollectCourse(@PathVariable("id") Long id) {
        return controllerHandler.messageHandler(() ->
                collectService.userCollectCourse(id));
    }


    @Operation(
            summary = "用户所有收藏的课程",
            description = "用户查询自己收藏的所有课程"
    )
    @GetMapping("/query_all")
    public RestBean<List<CourseVO>> queryAllCollectCourseId() {
        return collectService.getAllCollectCourse();
    }
}
