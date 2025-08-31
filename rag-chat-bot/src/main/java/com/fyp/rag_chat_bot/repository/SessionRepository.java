package com.fyp.rag_chat_bot.repository;

import com.fyp.rag_chat_bot.entity.SessionRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends CrudRepository<SessionRedis, String> {
    
    Optional<SessionRedis> findByConversationId(String conversationId);
    
    Optional<SessionRedis> findByConversationIdAndUserId(String conversationId, String userId);
    
    boolean existsByConversationId(String conversationId);
}