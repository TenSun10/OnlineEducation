package com.tenxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenxi.entity.po.Collect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CollectMapper extends BaseMapper<Collect> {
    int batchInsertIgnore(@Param("userId") Long userId, @Param("courseIds") List<Long> courseIds, @Param("collectTime") LocalDateTime collectTime, @Param("ids") List<Long> ids);

    List<Long> getCourseIdsByUserId(@Param("userId") Long userId);
}
