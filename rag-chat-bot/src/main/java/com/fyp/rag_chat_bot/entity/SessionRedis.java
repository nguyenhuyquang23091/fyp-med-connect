package com.fyp.rag_chat_bot.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;

@RedisHash(value = "session_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionRedis {

    @Id
    String sessionId;
    
    @Indexed
    String conversationId;
    
    @Indexed
    String userId;
    
    Instant createdAt;
    
    Instant lastUpdatedAt;
    
    @TimeToLive
    Long expirationTime;

}