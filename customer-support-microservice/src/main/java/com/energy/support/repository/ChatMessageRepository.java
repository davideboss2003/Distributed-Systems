package com.energy.support.repository;

import com.energy.support.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    
    @Query("SELECT c FROM ChatMessageEntity c WHERE c.fromUserId = ?1 OR c.toUserId = ?1 ORDER BY c.timestamp DESC")
    List<ChatMessageEntity> findByUserIdOrderByTimestampDesc(String userId);
    
    @Query("SELECT c FROM ChatMessageEntity c WHERE (c.fromUserId = ?1 AND c.toUserId = ?2) OR (c.fromUserId = ?2 AND c.toUserId = ?1) ORDER BY c.timestamp DESC")
    List<ChatMessageEntity> findConversationBetweenUsers(String userId1, String userId2);
}
