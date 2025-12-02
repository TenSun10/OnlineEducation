package com.tenxi.progress.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.progress.entity.dto.VideoProgressDTO;
import com.tenxi.progress.entity.po.VideoPlayRecord;
import com.tenxi.progress.entity.po.VideoProgress;
import com.tenxi.progress.entity.po.VideoWatchHistory;
import com.tenxi.progress.entity.vo.VideoProgressVO;
import com.tenxi.progress.mapper.VideoPlayRecordMapper;
import com.tenxi.progress.mapper.VideoProgressMapper;
import com.tenxi.progress.mapper.VideoWatchHistoryMapper;
import com.tenxi.progress.service.VideoProgressService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class VideoProgressServiceImpl implements VideoProgressService{
    @Resource
    private VideoProgressMapper videoProgressMapper;

    @Resource
    private VideoPlayRecordMapper videoPlayRecordMapper;

    @Resource
    private VideoWatchHistoryMapper videoWatchHistoryMapper;
    /**
     * 保存用户视频观看进度
     * @param videoProgressDTO
     * @return
     */
    @Override
    @Transactional
    public RestBean<String> postVideoProgress(VideoProgressDTO videoProgressDTO) {
        try {
            //1.更新或创建视频进度
            updateVideoProgress(videoProgressDTO);

            //2.更新每日播放记录
            updateDailyPlayRecord(videoProgressDTO);

            //3.更新观看历史
            updateWatchHistory(videoProgressDTO);

            return RestBean.successWithMsg("视频观看进度上传成功");

        }catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_INNER_ERROR, "进度保存失败: " + e.getMessage());
        }
    }



    /**
     * 根据id查询视频观看进度
     * @param id
     * @return
     */
    @Override
    public RestBean<VideoProgressVO> getProgressById(Long id) {
        Long userId = BaseContext.getCurrentId();

        VideoProgress videoProgress = videoProgressMapper.selectById(id);
        if (videoProgress == null || (long) userId != videoProgress.getUserId()) {
            throw new BusinessException(ErrorCode.VIDEO_PROGRESS_NOT_FOUND);
        }

        VideoProgressVO videoProgressVO = new VideoProgressVO();
        videoProgressVO.setVideoId(videoProgress.getVideoId());
        videoProgressVO.setUserId(userId);
        videoProgressVO.setProgressSecond(videoProgress.getProgressSecond());
        videoProgressVO.setTotalSecond(videoProgress.getTotalSecond());
        videoProgressVO.setPercentage(videoProgress.getPercentage());
        videoProgressVO.setId(videoProgress.getId());

        return RestBean.successWithData(videoProgressVO);
    }

    //-------------私有方法-----------
    /**
     * 更新视频播放进度
     */
    private void updateVideoProgress(VideoProgressDTO dto) {
        Long userId = dto.getUserId();
        Long videoId = dto.getVideoId();

        LambdaQueryWrapper<VideoProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoProgress::getVideoId, videoId);
        wrapper.eq(VideoProgress::getUserId, userId);

        VideoProgress videoProgress = videoProgressMapper.selectOne(wrapper);

        // 计算播放百分比
        BigDecimal percentage = calculatePercentage(dto.getProgressSecond(), dto.getTotalSecond());

        if (videoProgress != null) {
            videoProgress.setProgressSecond(dto.getProgressSecond());
            videoProgress.setTotalSecond(dto.getTotalSecond());
            videoProgress.setPercentage(percentage);
            videoProgress.setUpdateTime(LocalDateTime.now());

            if(videoProgressMapper.updateById(videoProgress) != 1) {
                throw new BusinessException(ErrorCode.SERVER_INNER_ERROR);
            }
        } else {
            // 创建新记录
            VideoProgress videoProgressToInsert = new VideoProgress();
            videoProgressToInsert.setVideoId(videoId);
            videoProgressToInsert.setUserId(userId);
            videoProgressToInsert.setProgressSecond(dto.getProgressSecond());
            videoProgressToInsert.setTotalSecond(dto.getTotalSecond());
            videoProgressToInsert.setPercentage(percentage);
            videoProgressToInsert.setUpdateTime(LocalDateTime.now());
            videoProgressToInsert.setCreateTime(LocalDateTime.now());

            if (videoProgressMapper.insert(videoProgressToInsert) != 1) {
                throw new BusinessException(ErrorCode.SERVER_INNER_ERROR);
            }
        }
    }


    /**
     * 更新每日播放记录（学习时长统计）
     */
    private void updateDailyPlayRecord(VideoProgressDTO videoProgressDTO) {
        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<VideoPlayRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoPlayRecord::getVideoId, videoProgressDTO.getVideoId());
        wrapper.eq(VideoPlayRecord::getUserId, videoProgressDTO.getUserId());
        wrapper.eq(VideoPlayRecord::getSessionDate, today);

        VideoPlayRecord record = videoPlayRecordMapper.selectOne(wrapper);

        // 计算本次观看时长（简化版：使用固定值或基于业务逻辑计算）
        Integer watchDuration = calculateWatchDuration(record, videoProgressDTO);

        if (record == null) {
            //创建记录
            record = new VideoPlayRecord();
            record.setVideoId(videoProgressDTO.getVideoId());
            record.setUserId(videoProgressDTO.getUserId());
            record.setSessionDate(today);
            record.setStartTime(LocalDateTime.now());
            record.setEndTime(LocalDateTime.now());
            record.setTotalWatchDuration(watchDuration);
            record.setProgressStart(0); // 第一次观看从0开始
            record.setProgressEnd(videoProgressDTO.getProgressSecond());
            record.setCreateTime(LocalDateTime.now());

            videoPlayRecordMapper.insert(record);
        }else {
            record.setEndTime(LocalDateTime.now());
            record.setTotalWatchDuration(Integer.sum(record.getTotalWatchDuration(), watchDuration));
            record.setProgressEnd(videoProgressDTO.getProgressSecond());

            videoPlayRecordMapper.updateById(record);
        }

    }

    /**
     * 更新观看历史
     */
    private void updateWatchHistory(VideoProgressDTO videoProgressDTO) {
        Long userId = videoProgressDTO.getUserId();
        Long videoId = videoProgressDTO.getVideoId();

        LambdaQueryWrapper<VideoWatchHistory> historyWrapper = new LambdaQueryWrapper<>();
        historyWrapper.eq(VideoWatchHistory::getUserId, userId)
                .eq(VideoWatchHistory::getVideoId, videoId);

        VideoWatchHistory history = videoWatchHistoryMapper.selectOne(historyWrapper);
        boolean completed = isVideoCompleted(videoProgressDTO.getProgressSecond(), videoProgressDTO.getTotalSecond());

        if (history == null) {
            // 创建新历史记录
            history = new VideoWatchHistory();
            history.setUserId(userId);
            history.setVideoId(videoId);

            // 获取对应的进度ID
            LambdaQueryWrapper<VideoProgress> progressWrapper = new LambdaQueryWrapper<>();
            progressWrapper.eq(VideoProgress::getUserId, userId)
                    .eq(VideoProgress::getVideoId, videoId);
            VideoProgress progress = videoProgressMapper.selectOne(progressWrapper);

            if (progress != null) {
                history.setProgressId(progress.getId());
            }

            history.setCreateTime(LocalDateTime.now());
            history.setLastWatchTime(LocalDateTime.now());
            history.setCompleted(completed);
            videoWatchHistoryMapper.insert(history);
        } else {
            // 更新最后观看时间
            history.setLastWatchTime(LocalDateTime.now());
            history.setCompleted(completed);
            videoWatchHistoryMapper.updateById(history);
        }
    }
    /**
    * 计算进度百分比
     */
    private BigDecimal calculatePercentage(Integer progressSecond, Integer totalSecond) {
        if (totalSecond == null || totalSecond == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(progressSecond)
                .divide(BigDecimal.valueOf(totalSecond), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)); //百分号表示表示进度
    }

    /**
     * 观看超过百分之95算观看完成
     */
    private boolean isVideoCompleted(Integer progressSecond, Integer totalSecond) {
        if (totalSecond == null || totalSecond == 0) {
            return false;
        }
        return progressSecond >= totalSecond * 0.95;
    }

    /**
     * 计算观看时长
     */
    private Integer calculateWatchDuration(VideoPlayRecord record, VideoProgressDTO videoProgressDTO) {
        if (videoProgressDTO.getStartTime() != null && videoProgressDTO.getEndTime() != null) {
            long seconds = java.time.Duration.between(
                    videoProgressDTO.getStartTime(),
                    videoProgressDTO.getEndTime()
            ).getSeconds();
            return (int) Math.max(seconds, 0); // 确保非负数
        }

        return 30;
    }
}
