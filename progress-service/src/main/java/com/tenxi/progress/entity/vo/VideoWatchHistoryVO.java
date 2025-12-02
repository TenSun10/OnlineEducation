package com.tenxi.progress.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoWatchHistoryVO {
    private Long id;
    private Long videoId;
    private Long userId;
    private Long progressId;
    private LocalDateTime lastWatchTime;
}
