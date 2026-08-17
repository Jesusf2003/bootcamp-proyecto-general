package com.banco.notifications.controller;

import com.banco.notifications.listener.NotificationStore;
import com.banco.notifications.model.Notification;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Expone las notificaciones simuladas, util para verificar visualmente el flujo de eventos. */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notificaciones generadas a partir de eventos Kafka")
public class NotificationController {

    private final NotificationStore notificationStore;

    public NotificationController(NotificationStore notificationStore) {
        this.notificationStore = notificationStore;
    }

    @GetMapping
    public List<Notification> getAll() {
        return notificationStore.getAll();
    }
}
