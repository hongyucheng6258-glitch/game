
package com.dianjing.controller;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.ChatMessageRequest;
import com.dianjing.dto.request.PriceNegotiationRequest;
import com.dianjing.dto.request.PriceNegotiationResponse;
import com.dianjing.dto.response.ConversationResponse;
import com.dianjing.entity.Conversation;
import com.dianjing.entity.Message;
import com.dianjing.service.ChatService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    @GetMapping("/conversations")
    public Result<List<ConversationResponse>> listConversations() {
        Long userId = getCurrentUserId();
        return Result.success(chatService.getUserConversations(userId));
    }

    @GetMapping("/{otherUserId}")
    public Result<PageResult<Message>> getChatHistory(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Message> messagePage = chatService.getChatHistory(userId, otherUserId, pageable);
        return Result.success(new PageResult<>(messagePage.getTotalElements(),
                messagePage.getTotalPages(), page, messagePage.getContent()));
    }

    @GetMapping("/history/{otherUserId}")
    public Result<PageResult<Message>> getChatHistoryByHistoryPath(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return getChatHistory(otherUserId, page, size);
    }

    @PostMapping("/send")
    public Result<Message> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        Long userId = getCurrentUserId();
        Message message = chatService.sendMessage(userId, request);
        return Result.success(message);
    }

    @PostMapping("/price-request")
    public Result<Message> sendPriceRequest(@Valid @RequestBody PriceNegotiationRequest request) {
        Long userId = getCurrentUserId();
        Message message = chatService.sendPriceRequest(userId, request);
        return Result.success(message);
    }

    @PostMapping("/price-response")
    public Result<Message> sendPriceResponse(@Valid @RequestBody PriceNegotiationResponse request) {
        Long userId = getCurrentUserId();
        Message message = chatService.sendPriceResponse(userId, request);
        return Result.success(message);
    }

    @PutMapping("/{otherUserId}/read")
    public Result<Void> markConversationAsRead(@PathVariable Long otherUserId) {
        Long userId = getCurrentUserId();
        chatService.markConversationAsRead(userId, otherUserId);
        return Result.success();
    }
}
