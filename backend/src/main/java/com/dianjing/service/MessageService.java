package com.dianjing.service;

import com.dianjing.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    Message sendSystemMessage(Long receiverId, String content);

    Message sendSystemMessage(Long receiverId, String content, Long relatedId, String relatedType);

    Page<Message> list(Long receiverId, Integer type, Integer isRead, Pageable pageable);

    void markRead(Long receiverId, Long messageId);

    void markAllRead(Long receiverId);

    long getUnreadCount(Long receiverId);

    long getUnreadSystemCount(Long receiverId);

    void deleteMessage(Long receiverId, Long messageId);
}
