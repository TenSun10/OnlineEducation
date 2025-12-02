package com.tenxi.progress.controller;

import com.tenxi.progress.entity.dto.VideoProgressDTO;
import com.tenxi.progress.entity.vo.VideoProgressVO;
import com.tenxi.progress.service.VideoProgressService;
import com.tenxi.utils.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/progress")
public class VideoProgressController {
    @Resource
    private VideoProgressService videoProgressService;

    @Operation(
            summary = "上传用户视频进度",
            description = "前端在视频暂停或者用户关闭视频的时候触发的视频进度上传",
            responses = {

            }
    )
    @PostMapping("/post")
    public RestBean<String> postVideoProgress(@RequestBody VideoProgressDTO videoProgressDTO) {
        log.info("上传用户视频观看记录...");
        return videoProgressService.postVideoProgress(videoProgressDTO);
    }

    @GetMapping("/{id}")
    public RestBean<VideoProgressVO> getVideoProgressById(@PathVariable("id") Long id) {
        log.info("根据id查询视频观看进度...");
        return videoProgressService.getProgressById(id);
    }
}
