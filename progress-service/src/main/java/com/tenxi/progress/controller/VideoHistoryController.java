package com.tenxi.progress.controller;

import com.tenxi.progress.entity.vo.VideoWatchHistoryVO;
import com.tenxi.progress.service.VideoWatchHistoryService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/history")
public class VideoHistoryController {
    @Resource
    private VideoWatchHistoryService videoWatchHistoryService;

    @Operation(
            summary = "查询用户所有的观看记录",
            description = "根据用户的id查询观看记录，其中返回了progressId，用于查询视频观看进度"
    )
    @GetMapping("/all")
    public RestBean<List<VideoWatchHistoryVO>> getAllVideoWatchHistory() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户：{}正在查询所有的观看记录...", userId);
        return videoWatchHistoryService.queryAllHistory(userId);
    }

    @Operation(
            summary = "删除用户观看记录",
            description = "根据记录id删除观看记录"

    )
    @DeleteMapping("/{id}")
    public RestBean<String> deleteVideoWatchHistory(@PathVariable("id") Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户：{}正在删除{}观看记录...", userId, id);

        return videoWatchHistoryService.deleteHistoryById(userId, id);

    }

}
