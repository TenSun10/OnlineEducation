package com.tenxi.progress.service;

import com.tenxi.progress.entity.vo.VideoWatchHistoryVO;
import com.tenxi.utils.RestBean;

import java.util.List;

public interface VideoWatchHistoryService {
    RestBean<List<VideoWatchHistoryVO>> queryAllHistory(Long userId);

    RestBean<String> deleteHistoryById(Long userId, Long id);
}
