package com.library.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Notification;
import com.library.mapper.NotificationMapper;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationMapper notificationMapper;

    @Override
    public void create(Long userId, String type, String title, String content, Long refId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRefId(refId);
        n.setIsRead(0);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    @Override
    public List<Notification> listByUser(Long userId) {
        return notificationMapper.selectList(new LambdaQueryWrapper<Notification>().eq(Notification::getUserId,userId).orderByDesc(Notification::getCreatedAt));
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n != null && n.getUserId().equals(userId)) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    @Override
    public int unreadCount(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>().eq(Notification::getUserId,userId).eq(Notification::getIsRead,0)).intValue();
    }
}
