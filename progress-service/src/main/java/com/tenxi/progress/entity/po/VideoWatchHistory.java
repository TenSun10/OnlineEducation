package com.tenxi.progress.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用来存放用户所有观看过的视频的历史记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("video_watch_history")
public class VideoWatchHistory {
    private Long id;
    private Long userId;
    private Long videoId;
    private Long progressId; //对应视频的播放记录，方便接着看
    private LocalDateTime createTime;
    private LocalDateTime lastWatchTime;
    private Boolean completed;
}
