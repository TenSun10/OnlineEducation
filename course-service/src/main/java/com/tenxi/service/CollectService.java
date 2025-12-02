package com.tenxi.service;

import com.tenxi.entity.vo.CourseVO;
import com.tenxi.utils.RestBean;

import java.util.List;

public interface CollectService {
    String userCollectCourse(Long id);

    RestBean<List<CourseVO>> getAllCollectCourse();
}
