package com.dianjing.controller;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.response.ConversationResponse;
import com.dianjing.entity.Message;
import com.dianjing.service.ChatService;
import com.dianjing.service.MessageService;
import com.dianjing.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final ChatService chatService;

    public MessageController(MessageService messageService, UserService userService, ChatService chatService) {
        this.messageService = messageService;
        this.userService = userService;
        this.chatService = chatService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    /**
     * 消息列表
     */
    @GetMapping
    public Result<PageResult<Message>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Message> messagePage = messageService.list(userId, type, isRead, pageable);
        return Result.success(new PageResult<>(messagePage.getTotalElements(),
                messagePage.getTotalPages(), page, messagePage.getContent()));
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        messageService.markRead(userId, id);
        return Result.success();
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        Long userId = getCurrentUserId();
        messageService.markAllRead(userId);
        return Result.success();
    }

    /**
     * 未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        Long userId = getCurrentUserId();
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", messageService.getUnreadCount(userId));
        counts.put("system", messageService.getUnreadSystemCount(userId));
        return Result.success(counts);
    }

    /**
     * 会话列表
     */
    @GetMapping("/conversations")
    public Result<List<ConversationResponse>> listConversations() {
        Long userId = getCurrentUserId();
        return Result.success(chatService.getUserConversations(userId));
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        messageService.deleteMessage(userId, id);
        return Result.success();
    }
}
