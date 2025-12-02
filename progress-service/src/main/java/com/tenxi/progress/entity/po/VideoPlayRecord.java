package com.tenxi.progress.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用来分片存储用户视频观看时长
 * 目前主要作用是计算用户观看时长（一定日期）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("video_play_record")
@Schema(description = "视频观看记录表")
public class VideoPlayRecord {
    private Long id;
    private Long userId;
    private Long videoId;
    private LocalDate sessionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalWatchDuration;
    private Integer progressStart;
    private Integer progressEnd;
    private LocalDateTime createTime;
}
