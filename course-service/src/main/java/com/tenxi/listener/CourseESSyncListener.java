package com.tenxi.listener;

import com.tenxi.entity.msg.CourseESSyncMessage;
import com.tenxi.service.CourseESSyncService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue(name = "course_sync_es_handler"),
                exchange = @Exchange(name = "online.edu.direct"),
                key = "course_sync_op"
        )
})
public class CourseESSyncListener {
    @Resource
    private CourseESSyncService courseESSyncService;

    @RabbitHandler
    public void handleCourseSync(CourseESSyncMessage message) {
        try {
            switch (message.getOperation()) {
                case CREATE:
                case UPDATE:
                    courseESSyncService.fullSyncCourseToES(message.getCourseId());
                    break;
                case DELETE:
                    courseESSyncService.deleteCourseFromES(message.getCourseId());
                    break;
            }
            log.info("课程信息同步完成，courseId：{}，operation：{}",
                    message.getCourseId(), message.getOperation());
        } catch (Exception e) {
            log.error("课程同步处理失败, courseId: {}", message.getCourseId(), e);
        }
    }
}
