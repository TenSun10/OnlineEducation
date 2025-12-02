package com.tenxi.progress.service;

import com.tenxi.progress.entity.vo.DateRangeLearningStatisticVO;
import com.tenxi.progress.entity.vo.TotalLearningStatisticVO;
import com.tenxi.progress.entity.vo.VideoLearningStatisticVO;
import com.tenxi.utils.RestBean;

import java.time.LocalDate;
import java.util.Map;

public interface LearningStatisticsService {
    RestBean<TotalLearningStatisticVO> getLearningOverview(Long userId);

    RestBean<DateRangeLearningStatisticVO> getLearningDurationByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    RestBean<VideoLearningStatisticVO> getVideoLearningStats(Long videoId);

    Integer getTotalLearningDuration(Long userId);
}
