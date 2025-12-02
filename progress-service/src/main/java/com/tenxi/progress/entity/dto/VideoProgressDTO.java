package com.tenxi.progress.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoProgressDTO {
    private Long videoId;
    private Long userId;
    private LocalDateTime startTime; //用户打开视频的时间
    private LocalDateTime endTime; //触发进度上传的时间
    private Integer progressSecond; //目前观看的视频的秒数
    private Integer totalSecond;
}
