package com.tenxi.job;

import com.tenxi.entity.msg.CourseESSyncMessage;
import com.tenxi.mapper.CourseMapper;
import com.tenxi.service.CourseESSyncService;
import com.tenxi.service.CourseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CourseESSyncJob {
    @Resource
    private CourseESSyncService courseESSyncService;
    @Resource
    private CourseMapper courseMapper;

    /***
     * 每天凌晨2点执行全量同步 - 作为兜底机制
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyFullSync() {
        log.info("开始定时全量同步课程数据到ES...");
        try {
            List<Long> allCourseIds = courseMapper.selectAllCourseIds();
            courseESSyncService.batchFullSyncCourseToES(allCourseIds);
            log.info("定时全量同步完成，共同步 {} 门课程", allCourseIds.size());
        } catch (Exception e) {
            log.error("定时全量同步失败", e);
        }
    }
}
