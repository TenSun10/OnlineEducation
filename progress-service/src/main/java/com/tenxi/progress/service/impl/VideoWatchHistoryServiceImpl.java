package com.tenxi.progress.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.progress.entity.po.VideoWatchHistory;
import com.tenxi.progress.entity.vo.VideoWatchHistoryVO;
import com.tenxi.progress.mapper.VideoWatchHistoryMapper;
import com.tenxi.progress.service.VideoWatchHistoryService;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoWatchHistoryServiceImpl implements VideoWatchHistoryService {
    @Resource
    private VideoWatchHistoryMapper videoWatchHistoryMapper;


    /**
     * 根据id查询所有观看记录
     * @param userId
     * @return
     */
    @Override
    public RestBean<List<VideoWatchHistoryVO>> queryAllHistory(Long userId) {
        LambdaQueryWrapper<VideoWatchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoWatchHistory::getUserId, userId);
        queryWrapper.orderByAsc(VideoWatchHistory::getLastWatchTime);

        List<VideoWatchHistory> histories = videoWatchHistoryMapper.selectList(queryWrapper);
        List<VideoWatchHistoryVO> historyVOS = transToVO(histories);

        return RestBean.successWithData(historyVOS);
    }

    /**
     * 根据用户id和id删除观看记录
     * @param userId
     * @param id
     * @return
     */
    @Override
    public RestBean<String> deleteHistoryById(Long userId, Long id) {
        VideoWatchHistory videoWatchHistory = videoWatchHistoryMapper.selectById(id);
        if (videoWatchHistory == null) {
            throw new BusinessException(ErrorCode.VIDEO_WATCH_HISTORY_NOT_FOUND);
        }

        if (videoWatchHistory.getUserId() != userId) {
            throw new BusinessException(ErrorCode.VIDEO_WATCH_HISTORY_OPERATION_NOT_AUTH);
        }

        if (videoWatchHistoryMapper.deleteById(id) <= 0) {
            throw new BusinessException(ErrorCode.SERVER_INNER_ERROR);
        }

        return RestBean.successWithMsg("记录删除成功");
    }


    //----------------私有方法-------------
    private List<VideoWatchHistoryVO> transToVO(List<VideoWatchHistory> histories) {
        return histories.stream().map(item -> {
                VideoWatchHistoryVO vo = new VideoWatchHistoryVO();
                vo.setId(item.getId());
                vo.setUserId(item.getUserId());
                vo.setVideoId(item.getVideoId());
                vo.setProgressId(item.getProgressId());
                vo.setLastWatchTime(item.getLastWatchTime());
                return vo;
            }
        ).toList();
    }
}
