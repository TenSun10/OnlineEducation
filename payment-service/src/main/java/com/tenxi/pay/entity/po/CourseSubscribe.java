package com.tenxi.pay.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("course_subscribe")
public class CourseSubscribe {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long courseId;
    private Long accountId;
    private Long orderId;
    private LocalDateTime subscribeTime;
}
