package com.dianjing.service;

import com.dianjing.dto.request.ChatMessageRequest;
import com.dianjing.dto.request.PriceNegotiationRequest;
import com.dianjing.dto.request.PriceNegotiationResponse;
import com.dianjing.dto.response.ConversationResponse;
import com.dianjing.entity.Conversation;
import com.dianjing.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {

    Conversation getOrCreateConversation(Long userId1, Long userId2);

    List<ConversationResponse> getUserConversations(Long userId);

    Page<Message> getChatHistory(Long userId1, Long userId2, Pageable pageable);

    Message sendMessage(Long senderId, ChatMessageRequest request);
    
    Message sendPriceRequest(Long userId, PriceNegotiationRequest request);
    
    Message sendPriceResponse(Long userId, PriceNegotiationResponse request);

    void markConversationAsRead(Long userId, Long otherUserId);
}
