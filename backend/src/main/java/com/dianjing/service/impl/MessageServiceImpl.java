package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.entity.Message;
import com.dianjing.mapper.MessageMapper;
import com.dianjing.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    public MessageServiceImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public Message sendSystemMessage(Long receiverId, String content) {
        return sendSystemMessage(receiverId, content, null, null);
    }

    @Override
    public Message sendSystemMessage(Long receiverId, String content, Long relatedId, String relatedType) {
        Message message = new Message();
        message.setReceiverId(receiverId);
        message.setType(Constants.MESSAGE_TYPE_SYSTEM);
        message.setContent(content);
        message.setIsRead(0);
        message.setRelatedId(relatedId);
        message.setRelatedType(relatedType);
        return messageMapper.save(message);
    }

    @Override
    public Page<Message> list(Long receiverId, Integer type, Integer isRead, Pageable pageable) {
        if (type != null && isRead != null) {
            return messageMapper.findByReceiverIdAndTypeAndIsReadOrderByIdDesc(receiverId, type, isRead, pageable);
        } else if (type != null) {
            return messageMapper.findByReceiverIdAndTypeOrderByIdDesc(receiverId, type, pageable);
        } else if (isRead != null) {
            return messageMapper.findByReceiverIdAndIsReadOrderByIdDesc(receiverId, isRead, pageable);
        }
        return messageMapper.findByReceiverIdOrderByIdDesc(receiverId, pageable);
    }

    @Override
    public void markRead(Long receiverId, Long messageId) {
        Message message = messageMapper.findById(messageId)
                .orElseThrow(() -> new BusinessException(404, "消息不存在"));
        if (!message.getReceiverId().equals(receiverId)) {
            throw new BusinessException(403, "无权操作此消息");
        }
        message.setIsRead(1);
        messageMapper.save(message);
    }

    @Override
    public void markAllRead(Long receiverId) {
        Page<Message> unreadMessages = messageMapper.findByReceiverIdAndIsReadOrderByIdDesc(
                receiverId, 0, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE));
        for (Message message : unreadMessages.getContent()) {
            message.setIsRead(1);
            messageMapper.save(message);
        }
    }

    @Override
    public long getUnreadCount(Long receiverId) {
        return messageMapper.countByReceiverIdAndIsRead(receiverId, 0);
    }

    @Override
    public long getUnreadSystemCount(Long receiverId) {
        return messageMapper.countByReceiverIdAndTypeAndIsRead(receiverId, Constants.MESSAGE_TYPE_SYSTEM, 0);
    }

    @Override
    public void deleteMessage(Long receiverId, Long messageId) {
        Message message = messageMapper.findById(messageId)
                .orElseThrow(() -> new BusinessException(404, "消息不存在"));
        if (!message.getReceiverId().equals(receiverId)) {
            throw new BusinessException(403, "无权操作此消息");
        }
        messageMapper.delete(message);
    }
}
