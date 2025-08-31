package com.fyp.rag_chat_bot.repository;

import com.fyp.rag_chat_bot.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostgresSessionRepository extends JpaRepository<SessionEntity, Long> {
    
    Optional<SessionEntity> findByConversationId(String conversationId);
    
    Optional<SessionEntity> findBySessionId(String sessionId);
    
    @Query("SELECT s FROM SessionEntity s WHERE s.conversationId = :conversationId AND s.userId = :userId")
    Optional<SessionEntity> findByConversationIdAndUserId(@Param("conversationId") String conversationId, @Param("userId") String userId);
    
    boolean existsByConversationId(String conversationId);

    void deleteBySessionId(String sessionId);
}