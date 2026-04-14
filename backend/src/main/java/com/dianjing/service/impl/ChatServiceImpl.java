package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.ChatMessageRequest;
import com.dianjing.dto.request.PriceNegotiationRequest;
import com.dianjing.dto.request.PriceNegotiationResponse;
import com.dianjing.dto.response.ConversationResponse;
import com.dianjing.entity.Conversation;
import com.dianjing.entity.Message;
import com.dianjing.entity.User;
import com.dianjing.mapper.ConversationMapper;
import com.dianjing.mapper.MessageMapper;
import com.dianjing.mapper.UserMapper;
import com.dianjing.service.ChatService;
import com.dianjing.service.ServiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ServiceService serviceService;
    private final ObjectMapper objectMapper;

    public ChatServiceImpl(ConversationMapper conversationMapper, MessageMapper messageMapper,
                           UserMapper userMapper, ServiceService serviceService) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
        this.serviceService = serviceService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Transactional
    public Conversation getOrCreateConversation(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            throw new BusinessException(400, "不能和自己聊天");
        }
        return conversationMapper.findConversation(userId1, userId2).orElseGet(() -> {
            Conversation conversation = new Conversation();
            conversation.setUser1Id(userId1);
            conversation.setUser2Id(userId2);
            conversation.setLastMessage("");
            conversation.setLastMessageTime(LocalDateTime.now());
            return conversationMapper.save(conversation);
        });
    }

    @Override
    public List<ConversationResponse> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationMapper.findUserConversations(userId);
        
        List<Long> otherUserIds = conversations.stream()
                .map(c -> c.getUser1Id().equals(userId) ? c.getUser2Id() : c.getUser1Id())
                .collect(Collectors.toList());
        
        Map<Long, User> userMap = new HashMap<>();
        if (!otherUserIds.isEmpty()) {
            List<User> users = userMapper.findAllById(otherUserIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }
        
        List<ConversationResponse> responses = new ArrayList<>();
        for (Conversation conv : conversations) {
            Long otherUserId = conv.getUser1Id().equals(userId) ? conv.getUser2Id() : conv.getUser1Id();
            User otherUser = userMap.get(otherUserId);
            
            long unreadCount = messageMapper.countByReceiverIdAndConversationIdAndIsRead(
                    userId, conv.getId(), 0);
            
            ConversationResponse resp = new ConversationResponse();
            resp.setId(conv.getId());
            resp.setUser1Id(conv.getUser1Id());
            resp.setUser2Id(conv.getUser2Id());
            resp.setLastMessage(conv.getLastMessage());
            resp.setLastMessageTime(conv.getLastMessageTime());
            resp.setUpdatedAt(conv.getUpdatedAt());
            resp.setOtherUserId(otherUserId);
            resp.setOtherUserName(otherUser != null ? otherUser.getUsername() : "用户");
            resp.setOtherUserAvatar(otherUser != null ? otherUser.getAvatar() : null);
            resp.setUnreadCount((int) unreadCount);
            
            responses.add(resp);
        }
        
        return responses;
    }

    @Override
    public Page<Message> getChatHistory(Long userId1, Long userId2, Pageable pageable) {
        return messageMapper.findChatHistory(userId1, userId2, pageable);
    }

    @Override
    @Transactional
    public Message sendMessage(Long senderId, ChatMessageRequest request) {
        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException(400, "不能给自己发消息");
        }

        // 获取或创建会话
        Conversation conversation = getOrCreateConversation(senderId, request.getReceiverId());

        // 创建聊天消息
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setConversationId(conversation.getId());
        message.setType(request.getType() != null ? request.getType() : Constants.MESSAGE_TYPE_CHAT);
        message.setContent(request.getContent());
        message.setIsRead(0);
        message = messageMapper.save(message);

        // 更新会话最后消息
        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationMapper.save(conversation);

        return message;
    }
    
    @Override
    @Transactional
    public Message sendPriceRequest(Long userId, PriceNegotiationRequest request) {
        com.dianjing.entity.Service service = serviceService.getById(request.getServiceId());
        if (service == null) {
            throw new BusinessException(404, "服务不存在");
        }
        
        Long receiverId = service.getProviderId();
        if (userId.equals(receiverId)) {
            throw new BusinessException(400, "不能向自己发送价格请求");
        }
        
        // 获取或创建会话
        Conversation conversation = getOrCreateConversation(userId, receiverId);
        
        // 先创建消息获取ID（使用临时内容）
        Message message = new Message();
        message.setSenderId(userId);
        message.setReceiverId(receiverId);
        message.setConversationId(conversation.getId());
        message.setType(Constants.MESSAGE_TYPE_PRICE_REQUEST);
        message.setContent("");
        message.setIsRead(0);
        message = messageMapper.save(message);
        
        // 构建完整的JSON内容，包含消息ID
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("serviceId", request.getServiceId());
        contentMap.put("serviceTitle", service.getTitle());
        contentMap.put("originalPrice", service.getPrice());
        contentMap.put("requestedPrice", request.getRequestedPrice());
        contentMap.put("remark", request.getRemark());
        contentMap.put("requestMessageId", message.getId());
        
        String content;
        try {
            content = objectMapper.writeValueAsString(contentMap);
        } catch (Exception e) {
            throw new BusinessException(500, "消息格式错误");
        }
        
        // 更新消息内容
        message.setContent(content);
        message = messageMapper.save(message);
        
        // 更新会话最后消息
        conversation.setLastMessage("[价格请求] " + service.getTitle());
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationMapper.save(conversation);
        
        return message;
    }
    
    @Override
    @Transactional
    public Message sendPriceResponse(Long userId, PriceNegotiationResponse request) {
        Message requestMessage = messageMapper.findById(request.getRequestMessageId())
                .orElseThrow(() -> new BusinessException(404, "请求消息不存在"));
        
        if (!requestMessage.getType().equals(Constants.MESSAGE_TYPE_PRICE_REQUEST)) {
            throw new BusinessException(400, "无效的请求消息类型");
        }
        
        Long receiverId = requestMessage.getSenderId();
        if (userId.equals(receiverId)) {
            throw new BusinessException(400, "不能给自己发送价格响应");
        }
        
        com.dianjing.entity.Service service = serviceService.getById(request.getServiceId());
        if (service == null) {
            throw new BusinessException(404, "服务不存在");
        }
        
        // 获取或创建会话
        Conversation conversation = getOrCreateConversation(userId, receiverId);
        
        // 构建JSON内容
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("serviceId", request.getServiceId());
        contentMap.put("serviceTitle", service.getTitle());
        contentMap.put("negotiatedPrice", request.getNegotiatedPrice());
        contentMap.put("requestMessageId", request.getRequestMessageId());
        contentMap.put("remark", request.getRemark());
        
        String content;
        try {
            content = objectMapper.writeValueAsString(contentMap);
        } catch (Exception e) {
            throw new BusinessException(500, "消息格式错误");
        }
        
        // 创建消息
        Message message = new Message();
        message.setSenderId(userId);
        message.setReceiverId(receiverId);
        message.setConversationId(conversation.getId());
        message.setType(Constants.MESSAGE_TYPE_PRICE_RESPONSE);
        message.setContent(content);
        message.setIsRead(0);
        message = messageMapper.save(message);
        
        // 更新会话最后消息
        conversation.setLastMessage("[价格协商] " + service.getTitle());
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationMapper.save(conversation);
        
        return message;
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long userId, Long otherUserId) {
        Conversation conversation = conversationMapper.findConversation(userId, otherUserId)
                .orElse(null);
        if (conversation == null) {
            return;
        }
        
        List<Message> unreadMessages = messageMapper.findByReceiverIdAndConversationIdAndIsRead(
                userId, conversation.getId(), 0);
        for (Message msg : unreadMessages) {
            msg.setIsRead(1);
            messageMapper.save(msg);
        }
    }
}
