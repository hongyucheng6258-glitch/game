package com.dianjing.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationResponse {
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private LocalDateTime updatedAt;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private Integer unreadCount;
}
