package com.fyp.rag_chat_bot.services;


import com.fyp.rag_chat_bot.repository.PostgresSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionCleanService {
    PostgresSessionRepository postgresSessionRepository;
    RagService ragService;
    static final long ONE_HOUR = 3600000L;
    static final long TWO_HOUR_INACTIVE_THRESHOLD = 7200L;
    @Scheduled(fixedRate = ONE_HOUR)
    public void cleanUpInactiveSessions(){
        Instant inactiveThreshold = Instant.now().minusSeconds(TWO_HOUR_INACTIVE_THRESHOLD);
        try {
            List<String> conversations = postgresSessionRepository.findInactiveSessions(inactiveThreshold);

            for(String conversationId : conversations){
                ragService.deleteConversation(conversationId);
                log.info("Clean up {}", conversationId);
            }

            int deletedCount = postgresSessionRepository.deleteInactiveSessions(inactiveThreshold);
              log.info("Cleaned up {} inactive sessions older than 2 hours", deletedCount);
        } catch (Exception e) {
            log.error("Error during session cleanup", e);
        }
    }
}
