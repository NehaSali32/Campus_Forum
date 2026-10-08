package com.campusforum.service;

import com.campusforum.model.Notification;
import com.campusforum.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getUserNotifications(Long userId) {
        List<Notification> userSpecific = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Notification> broadcasts = notificationRepository.findByUserIdOrderByCreatedAtDesc(0L);

        List<Notification> combined = new ArrayList<>();
        combined.addAll(userSpecific);
        combined.addAll(broadcasts);
        combined.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        if (combined.size() > 20) {
            return combined.subList(0, 20);
        }
        return combined;
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);
    }

    public Notification createNotification(Long userId, String title, String message, String type, Long refId) {
        Notification notification = new Notification(userId, title, message, type, refId);
        return notificationRepository.save(notification);
    }
}
