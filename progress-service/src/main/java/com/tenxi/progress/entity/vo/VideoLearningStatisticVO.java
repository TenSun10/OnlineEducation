package com.tenxi.progress.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoLearningStatisticVO {
    private Long totalViews; //总观看次数
    private Long uniqueViewers; //总观看人数

    private Double averageCompletionRate; //平均播放完成率
    private Long completionCount; //完播数

    private Integer totalWatchDuration; //总观看时间
}
