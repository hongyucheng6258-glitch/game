package com.dianjing.websocket;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {

    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private LocalDateTime timestamp;
    private String type;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
        this.type = "CHAT";
    }
}
