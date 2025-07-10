package com.tenxi.notification.client;

import com.tenxi.config.FeignClientConfig;
import com.tenxi.notification.entity.vo.CourseSimpleVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "OE-course-service",
        configuration = {FeignClientConfig.class}
)
public interface CourseClient {
    @GetMapping({"/course/simple/{id}"})
    CourseSimpleVO getCourse(@PathVariable Long id);

    @GetMapping("/course/subscribe/{id}")
    List<Long> getCourseSubscribers(@PathVariable Long id);
}
