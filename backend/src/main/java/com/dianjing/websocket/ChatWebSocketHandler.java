package com.dianjing.websocket;

import com.dianjing.dto.request.ChatMessageRequest;
import com.dianjing.entity.Message;
import com.dianjing.service.ChatService;
import com.dianjing.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ChatService chatService;
    private final UserService userService;

    public ChatWebSocketHandler(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    /**
     * 处理聊天消息发送
     * 客户端发送到 /app/chat/send
     * 消息会通过 WebSocket 推送给接收者
     */
    @MessageMapping("/chat/send")
    @SendToUser("/queue/chat")
    public ChatMessage handleChatMessage(@Payload ChatMessage chatMessage, Principal principal) {
        try {
            String username = principal.getName();
            Long senderId = userService.getUserByUsername(username).getId();

            // Create the message via chat service
            ChatMessageRequest request = new ChatMessageRequest();
            request.setReceiverId(chatMessage.getReceiverId());
            request.setContent(chatMessage.getContent());

            Message savedMessage = chatService.sendMessage(senderId, request);

            // Build response
            ChatMessage response = new ChatMessage();
            response.setSenderId(savedMessage.getSenderId());
            response.setSenderName(username);
            response.setReceiverId(savedMessage.getReceiverId());
            response.setContent(savedMessage.getContent());
            response.setTimestamp(savedMessage.getCreatedAt());
            response.setType("CHAT");

            log.info("Chat message sent from {} to {}: {}", senderId, chatMessage.getReceiverId(), chatMessage.getContent());

            return response;
        } catch (Exception e) {
            log.error("Error handling chat message: {}", e.getMessage());
            ChatMessage errorResponse = new ChatMessage();
            errorResponse.setType("ERROR");
            errorResponse.setContent(e.getMessage());
            return errorResponse;
        }
    }
}
