package com.tenxi.progress.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "视频观看进度实体")
@TableName("video_progress")
public class VideoProgress {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long videoId;
    private Integer progressSecond;
    private Integer totalSecond;
    private BigDecimal percentage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
