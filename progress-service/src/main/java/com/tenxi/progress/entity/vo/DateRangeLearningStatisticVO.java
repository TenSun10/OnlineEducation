package com.tenxi.progress.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateRangeLearningStatisticVO {
    private List<Map<String, Object>> dailyStatistics; //按天数统计每天的数据
    private Map<String, Object> totalStatistics; //统计这段时间内的总数据
    private LocalDate startDate;
    private LocalDate endDate;
}
