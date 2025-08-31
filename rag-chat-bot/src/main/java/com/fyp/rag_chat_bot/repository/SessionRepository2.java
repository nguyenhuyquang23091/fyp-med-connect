package com.fyp.rag_chat_bot.repository;

import com.fyp.rag_chat_bot.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<ChatSession, String> {

    @Query("SELECT s FROM ChatSession s WHERE s.conversationId = :conversationId")
    Optional<ChatSession> findByConversationId(@Param("conversationId") String conversationId);

}
