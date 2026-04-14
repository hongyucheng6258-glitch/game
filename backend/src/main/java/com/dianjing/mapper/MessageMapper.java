package com.dianjing.mapper;

import com.dianjing.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageMapper extends JpaRepository<Message, Long> {
    Page<Message> findByReceiverIdOrderByIdDesc(Long receiverId, Pageable pageable);
    Page<Message> findByReceiverIdAndTypeOrderByIdDesc(Long receiverId, Integer type, Pageable pageable);
    Page<Message> findByReceiverIdAndIsReadOrderByIdDesc(Long receiverId, Integer isRead, Pageable pageable);
    Page<Message> findByReceiverIdAndTypeAndIsReadOrderByIdDesc(Long receiverId, Integer type, Integer isRead, Pageable pageable);
    long countByReceiverIdAndIsRead(Long receiverId, Integer isRead);
    long countByReceiverIdAndTypeAndIsRead(Long receiverId, Integer type, Integer isRead);

    @org.springframework.data.jpa.repository.Query("SELECT m FROM Message m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
           "ORDER BY m.id DESC")
    Page<Message> findChatHistory(@Param("userId1") Long userId1,
                                   @Param("userId2") Long userId2,
                                   Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :receiverId AND m.conversationId = :conversationId AND m.isRead = :isRead")
    long countByReceiverIdAndConversationIdAndIsRead(@Param("receiverId") Long receiverId,
                                                        @Param("conversationId") Long conversationId,
                                                        @Param("isRead") Integer isRead);

    @org.springframework.data.jpa.repository.Query("SELECT m FROM Message m WHERE m.receiverId = :receiverId AND m.conversationId = :conversationId AND m.isRead = :isRead")
    List<Message> findByReceiverIdAndConversationIdAndIsRead(@Param("receiverId") Long receiverId,
                                                               @Param("conversationId") Long conversationId,
                                                               @Param("isRead") Integer isRead);
}
