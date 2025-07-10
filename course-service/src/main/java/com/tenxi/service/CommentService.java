package com.tenxi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.entity.dto.CommentAddDTO;
import com.tenxi.entity.po.Comment;
import com.tenxi.entity.vo.CommentVO;
import com.tenxi.utils.RestBean;

import java.util.List;

public interface CommentService extends IService<Comment> {
    RestBean<Long> publishComment(CommentAddDTO commentAddDTO);

    String deleteCommentById(Long id);

    RestBean<Boolean> judgeComment(Long id);

    RestBean<List<CommentVO>> getCourseComments(Long id);
}
