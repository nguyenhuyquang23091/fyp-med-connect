package com.fyp.statistic_service.service;


import com.fyp.statistic_service.entity.SessionEntity;
import com.fyp.statistic_service.exceptions.AppException;
import com.fyp.statistic_service.exceptions.ErrorCode;
import com.fyp.statistic_service.repository.PostgresSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatSessionService {
    PostgresSessionRepository postgresSessionRepository;

    public SessionEntity findOrCreateSession(String conversationId, String userId) {
        if(conversationId != null && !conversationId.isEmpty()){
            // Check PostgreSQL for existing session
            Optional<SessionEntity> dbSession = postgresSessionRepository.findByConversationIdAndUserId(conversationId, userId);
            if(dbSession.isPresent()){
                SessionEntity session =  dbSession.get();
                session.setLastUpdatedAt(Instant.now());
                return postgresSessionRepository.save(session);
            }
        }
        // Create new conversation if not found
        return createNewSession(userId);
    }

    private SessionEntity createNewSession(String userId){

        String newSessionId = UUID.randomUUID().toString();
        String newConversationId = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();
        Instant lastUpdatedAt = Instant.now();

        SessionEntity sessionEntity = SessionEntity
                .builder()
                .sessionId(newSessionId)
                .conversationId(newConversationId)
                .userId(userId)
                .createdAt(createdAt)
                .lastUpdatedAt(lastUpdatedAt)
                .build();

        // Save to PostgreSQL for persistence
        SessionEntity savedSession = postgresSessionRepository.save(sessionEntity);

        return savedSession;
    }


    protected void validateSessionInfo(String conversationId, String userId){
        postgresSessionRepository.findByConversationIdAndUserId
                (conversationId, userId).orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }


}
