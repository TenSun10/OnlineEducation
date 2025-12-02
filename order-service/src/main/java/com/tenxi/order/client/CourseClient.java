package com.tenxi.order.client;

import com.tenxi.config.FeignClientConfig;
import com.tenxi.order.entity.vo.CourseSimpleVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "OE-course-service",
        contextId = "orderCourseClient",
        configuration = FeignClientConfig.class)
public interface CourseClient {
    @GetMapping("/course/simple/{id}")
   CourseSimpleVO getCourse(@PathVariable("id") Long id);
}
