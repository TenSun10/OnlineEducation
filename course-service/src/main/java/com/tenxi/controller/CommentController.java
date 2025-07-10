package com.tenxi.controller;

import com.tenxi.entity.dto.CommentAddDTO;
import com.tenxi.entity.vo.CommentVO;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.service.CommentService;
import com.tenxi.utils.RestBean;
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
    @PostMapping("/publish")
    public RestBean<Long> publishComment(@RequestBody CommentAddDTO commentAddDTO) {
        return commentService.publishComment(commentAddDTO);
    }

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
    @GetMapping("/judge/{id}")
    public RestBean<Boolean> judge(@PathVariable("id") Long id) {
        return commentService.judgeComment(id);
    }

    @GetMapping("/course/{id}")
    public RestBean<List<CommentVO>> getCourseComments(@PathVariable("id") Long id) {
        return commentService.getCourseComments(id);
    }
}
