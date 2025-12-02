package com.tenxi.progress.service;

import com.tenxi.progress.entity.dto.VideoProgressDTO;
import com.tenxi.progress.entity.vo.VideoProgressVO;
import com.tenxi.utils.RestBean;

public interface VideoProgressService {
    RestBean<String> postVideoProgress(VideoProgressDTO videoProgressDTO);

    RestBean<VideoProgressVO> getProgressById(Long id);
}
