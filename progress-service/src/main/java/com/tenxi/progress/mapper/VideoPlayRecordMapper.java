package com.tenxi.progress.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenxi.progress.entity.po.VideoPlayRecord;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface VideoPlayRecordMapper extends BaseMapper<VideoPlayRecord> {

    Long countDistinctSessionDateByUserIdAndDateRange(@Param("userId") Long userId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    List<Map<String, Object>> selectDailyStatsByDateRange(@Param("userId") Long userId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);


    Map<String, Object> selectTotalStatsByDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    Integer sumWatchDurationByVideoId(Long videoId);
}
