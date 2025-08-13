package com.tenxi.controller;

import com.tenxi.entity.dto.CommentAddDTO;
import com.tenxi.entity.vo.CommentVO;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.service.CommentService;
import com.tenxi.utils.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Resource
@RequestMapping("/comments")
public class CommentController {
    @Resource
    private CommentService commentService;
    @Resource
    private ControllerHandler controllerHandler;

    /**
     * 发布评论
     * @param commentAddDTO
     * @return
     */
    @Operation(
            summary = "用户发布评论",
            description = "用户作为楼主或者回复发布评论"
    )
    @PostMapping("/publish")
    public RestBean<Long> publishComment(@RequestBody CommentAddDTO commentAddDTO) {
        return commentService.publishComment(commentAddDTO);
    }


    @Operation(
            summary = "用户删除评论",
            description = "发布评论的或者课程发布者删除评论"
    )
    @DeleteMapping("/delete/{id}")
    public RestBean<String> deleteComment(@PathVariable("id") Long id) {
        return controllerHandler.messageHandler(() ->
                commentService.deleteCommentById(id));
    }

    /**
     * 用于判断当前的用户是否有权限对评论进行操作
     * @param id
     * @return
     */
    @Operation(
            summary = "根据评论id判断当前用户是否对该评论操作权限",
            description = "便于前端提前判断用户是否有操作权限"
    )
    @GetMapping("/judge/{id}")
    public RestBean<Boolean> judge(@PathVariable("id") Long id) {
        return commentService.judgeComment(id);
    }


    @Operation(
            summary = "获取课程的所有评论",
            description = "用户或者课程发布者查看所有的评论"
    )
    @GetMapping("/course/{id}")
    public RestBean<List<CommentVO>> getCourseComments(@PathVariable("id") Long id) {
        return commentService.getCourseComments(id);
    }
}
