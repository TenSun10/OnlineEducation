package com.tenxi.progress.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tenxi.progress.entity.po.VideoPlayRecord;
import com.tenxi.progress.entity.po.VideoProgress;
import com.tenxi.progress.entity.po.VideoWatchHistory;
import com.tenxi.progress.entity.vo.DateRangeLearningStatisticVO;
import com.tenxi.progress.entity.vo.TotalLearningStatisticVO;
import com.tenxi.progress.entity.vo.VideoLearningStatisticVO;
import com.tenxi.progress.mapper.VideoPlayRecordMapper;
import com.tenxi.progress.mapper.VideoProgressMapper;
import com.tenxi.progress.mapper.VideoWatchHistoryMapper;
import com.tenxi.progress.service.LearningStatisticsService;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class LearningStatisticsServiceImpl implements LearningStatisticsService {
    @Resource
    private VideoPlayRecordMapper videoPlayRecordMapper;
    @Resource
    private VideoWatchHistoryMapper videoWatchHistoryMapper;
    @Autowired
    private VideoProgressMapper videoProgressMapper;


    /**
     * 获取用户的全部学习数据统计
     * @param userId
     * @return
     */
    @Override
    public RestBean<TotalLearningStatisticVO> getLearningOverview(Long userId) {
        TotalLearningStatisticVO vo = new TotalLearningStatisticVO();

        //1.总学习时长
        Integer totalLearningDuration = getTotalLearningDuration(userId);
        vo.setTotalLearningSeconds(totalLearningDuration);
        vo.setTotalLearningMinutes(totalLearningDuration / 60);
        vo.setTotalLearningHours(totalLearningDuration / 3600);

        //2.观看视频个数
        Long watchedVideoCount = videoWatchHistoryMapper.selectCount(
                new LambdaQueryWrapper<VideoWatchHistory>().eq(VideoWatchHistory::getUserId, userId));
        vo.setWatchedVideoCount(watchedVideoCount);

        //3.完成观看视频个数
        Long completedVideoCount = videoWatchHistoryMapper.selectCount(
                new LambdaQueryWrapper<VideoWatchHistory>().eq(VideoWatchHistory::getUserId, userId)
                        .eq(VideoWatchHistory::getCompleted, true)
        );
        vo.setCompletedVideosCount(completedVideoCount);

        //4.今日学习时长
        LambdaQueryWrapper<VideoPlayRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoPlayRecord::getUserId, userId);
        queryWrapper.eq(VideoPlayRecord::getSessionDate, LocalDate.now());
        queryWrapper.select(VideoPlayRecord::getTotalWatchDuration);
        VideoPlayRecord record = videoPlayRecordMapper.selectOne(queryWrapper);
        if (record != null) {
            vo.setTodayLearningSeconds(record.getTotalWatchDuration());
        }else {
            vo.setTodayLearningSeconds(0);
        }

        //5.最近7天学习天数
        Long continuousDays = countContinuousLearningDays(userId, 7);
        vo.setContinuesLearningDay(continuousDays);

        return RestBean.successWithData(vo);

    }


    /**
     * 获取用户时间段内的学习数据
     * @param userId
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public RestBean<DateRangeLearningStatisticVO> getLearningDurationByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        DateRangeLearningStatisticVO vo = new DateRangeLearningStatisticVO();

        // 1.按天统计的学习时长
        List<Map<String, Object>> dailyStats = videoPlayRecordMapper.selectDailyStatsByDateRange(
                userId, startDate, endDate
        );

        // 2.总统计
        Map<String, Object> totalStats = videoPlayRecordMapper.selectTotalStatsByDateRange(
                userId, startDate, endDate
        );

        vo.setDailyStatistics(dailyStats);
        vo.setTotalStatistics(totalStats);
        vo.setStartDate(startDate);
        vo.setEndDate(endDate);

        return RestBean.successWithData(vo);
    }

    /**
     * 获取指定视频的学习情况统计
     * @param videoId
     * @return
     */
    @Override
    public RestBean<VideoLearningStatisticVO> getVideoLearningStats(Long videoId) {
        VideoLearningStatisticVO vo = new VideoLearningStatisticVO();

        //1.计算视频观看次数
        Long totalViews = videoPlayRecordMapper.selectCount(
                new LambdaQueryWrapper<VideoPlayRecord>().eq(VideoPlayRecord::getVideoId, videoId)
        );
        vo.setTotalViews(totalViews);

        //2.独立观看人数
        Long uniqueViewer = videoPlayRecordMapper.selectCount(
                new LambdaQueryWrapper<VideoPlayRecord>()
                        .eq(VideoPlayRecord::getVideoId, videoId)
                        .select(VideoPlayRecord::getUserId)
                        .groupBy(VideoPlayRecord::getUserId)
        );
        vo.setUniqueViewers(uniqueViewer);

        //3.平均播放数据
        List<VideoProgress> progresses = videoProgressMapper.selectList(
                new LambdaQueryWrapper<VideoProgress>()
                        .eq(VideoProgress::getVideoId, videoId)
                        .select(VideoProgress::getPercentage)
        );
        if (!progresses.isEmpty()) {
            double avgCompletion = progresses.stream()
                    .mapToDouble(p -> p.getPercentage().doubleValue())
                    .average()
                    .orElse(0.0);

            vo.setAverageCompletionRate(Math.round(avgCompletion * 100) / 100.0);
        }else {
            vo.setAverageCompletionRate(0.0);
        }

        //4.播放完次数
        Long completionCount = videoWatchHistoryMapper.selectCount(
                new LambdaQueryWrapper<VideoWatchHistory>()
                        .eq(VideoWatchHistory::getVideoId, videoId)
                        .eq(VideoWatchHistory::getCompleted, true)
        );
        vo.setCompletionCount(completionCount);

        // 5.总观看时长
        Integer totalWatchDuration = videoPlayRecordMapper.sumWatchDurationByVideoId(videoId);
        vo.setTotalWatchDuration(totalWatchDuration);

        return RestBean.successWithData(vo);
    }

    @Override
    public Integer getTotalLearningDuration(Long userId) {
        LambdaQueryWrapper<VideoPlayRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoPlayRecord::getUserId, userId);
        queryWrapper.select(VideoPlayRecord::getTotalWatchDuration); //其他字段为null

        List<VideoPlayRecord> videoPlayRecords = videoPlayRecordMapper.selectList(queryWrapper);

        return videoPlayRecords.stream()
                .mapToInt(VideoPlayRecord::getTotalWatchDuration)
                .sum();
    }


    private Long countContinuousLearningDays(Long userId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        return videoPlayRecordMapper.countDistinctSessionDateByUserIdAndDateRange(userId, startDate, endDate);

    }
}
