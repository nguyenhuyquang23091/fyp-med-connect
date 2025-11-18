package com.fyp.rag_chat_bot.repository;

import com.fyp.rag_chat_bot.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostgresSessionRepository extends JpaRepository<SessionEntity, Long> {

    
    @Query("SELECT s FROM SessionEntity s WHERE s.conversationId = :conversationId AND s.userId = :userId")
    Optional<SessionEntity> findByConversationIdAndUserId(@Param("conversationId") String conversationId, @Param("userId") String userId);



    @Query("SELECT s.conversationId FROM SessionEntity s WHERE s.lastUpdatedAt < :threshold")
    List<String> findInactiveSessions(@Param("threshold") Instant threshold);

    @Modifying
    @Transactional
    @Query("DELETE FROM SessionEntity s WHERE s.lastUpdatedAt < :threshold")
    int deleteInactiveSessions(@Param("threshold") Instant threshold);


}