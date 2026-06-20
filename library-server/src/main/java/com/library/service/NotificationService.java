package com.library.service;
import com.library.entity.Notification;
import java.util.List;
public interface NotificationService {
    void create(Long userId, String type, String title, String content, Long refId);
    List<Notification> listByUser(Long userId);
    void markRead(Long userId, Long notificationId);
    int unreadCount(Long userId);
}