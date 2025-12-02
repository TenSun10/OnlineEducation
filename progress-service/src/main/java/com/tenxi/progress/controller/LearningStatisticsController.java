package com.tenxi.progress.controller;

import com.tenxi.progress.entity.vo.DateRangeLearningStatisticVO;

import com.tenxi.progress.entity.vo.TotalLearningStatisticVO;
import com.tenxi.progress.entity.vo.VideoLearningStatisticVO;
import com.tenxi.progress.service.LearningStatisticsService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/learning")
public class LearningStatisticsController {

    @Resource
    private LearningStatisticsService learningStatisticsService;

    @Operation(
            summary = "获取学习概况",
            description = "获取用户总学习时长、今日学习时长、连续学习天数等"
    )
    @GetMapping("/overview")
    public RestBean<TotalLearningStatisticVO> getLearningOverview() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户：{}正在查询学习概况...", userId);
        return learningStatisticsService.getLearningOverview(userId);
    }


    @Operation(
            summary = "获取学习时长统计",
            description = "获取指定日期范围内的详细学习时长统计"
    )
    @GetMapping("/duration")
    public RestBean<DateRangeLearningStatisticVO> getLearningDuration(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户：{}正在查询{}到{}的学习时长统计...", userId, startDate, endDate);

        return learningStatisticsService.getLearningDurationByDateRange(userId, startDate, endDate);
    }

    @Operation(
            summary = "获取视频学习统计",
            description = "获取指定视频的学习统计数据"
    )
    @GetMapping("/video/{videoId}")
    public RestBean<VideoLearningStatisticVO> getLearningVideoStats(@PathVariable("videoId") Long videoId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户{}正在查询视频{}的学习统计...",userId, videoId);

        return learningStatisticsService.getVideoLearningStats(videoId);
    }

    @GetMapping("/total-duration")
    public RestBean<Integer> getLearningTotalLearningDuration() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户{}正在查询总的学习时长", userId);

        Integer totalDuration = learningStatisticsService.getTotalLearningDuration(userId);
        return RestBean.successWithData(totalDuration);

    }

}
