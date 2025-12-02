package com.tenxi.progress.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalLearningStatisticVO {
    private Integer totalLearningSeconds;
    private Integer totalLearningMinutes;
    private Integer totalLearningHours;

    private Long watchedVideoCount;
    private Long completedVideosCount;

    private Integer todayLearningSeconds;

    private Long continuesLearningDay; //最近七天学习天数


}
