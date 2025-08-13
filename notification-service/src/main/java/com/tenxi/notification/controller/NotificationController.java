package com.tenxi.notification.controller;

import com.tenxi.handler.ControllerHandler;
import com.tenxi.notification.entity.dto.NotificationTypeAddDTO;
import com.tenxi.notification.entity.vo.NotificationVO;
import com.tenxi.notification.service.NotificationTypeService;
import com.tenxi.notification.service.NotificationService;
import com.tenxi.utils.RestBean;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/notify"})
public class NotificationController {
    @Resource
    private ControllerHandler controllerHandler;
    @Resource
    private NotificationTypeService notificationTypeService;
    @Resource
    private NotificationService notificationService;


    @Operation(
            summary = "新增通知模板操作",
            description = "管理员新增通知模板"
    )
    @PostMapping({"/types"})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public RestBean<String> createNotificationType(@RequestBody NotificationTypeAddDTO notificationType) {
        return this.controllerHandler.messageHandler(() ->
            notificationTypeService.addNotificationType(notificationType)
        );
    }

    /**
     * 修改通知是否已读的操作
     * @param id
     * @return
     */
    @Hidden
    @GetMapping("/read/{id}")
    public RestBean<String> markAsRead(@PathVariable Long id) {
        return controllerHandler.messageHandler(() ->
                notificationService.markAsRead(id));
    }

    @Operation(
            summary = "获取通知",
            description = "通过通知的id获取到通知的详情信息"
    )
    @GetMapping("/{id}")
    public RestBean<NotificationVO> getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationVOById(id);
    }
}