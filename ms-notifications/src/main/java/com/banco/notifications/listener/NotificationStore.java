package com.banco.notifications.listener;

import com.banco.notifications.model.Notification;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Almacen en memoria de las ultimas notificaciones procesadas, solo
 * para poder verificarlas via GET /api/v1/notifications durante las
 * pruebas (en un entorno real se enviarian por correo/SMS/push y no
 * se persistirian aqui).
 */
@Component
public class NotificationStore {

    private static final int MAX_SIZE = 200;
    private final LinkedList<Notification> notifications = new LinkedList<>();

    public synchronized void add(Notification notification) {
        notifications.addFirst(notification);
        if (notifications.size() > MAX_SIZE) {
            notifications.removeLast();
        }
    }

    public synchronized List<Notification> getAll() {
        return Collections.unmodifiableList(new LinkedList<>(notifications));
    }
}
