package com.fyp.rag_chat_bot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "session_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @Column(unique = true, nullable = false)
    String sessionId;
    
    @Column(nullable = false)
    String userId;
    
    @Column(unique = true, nullable = false)
    String conversationId;
    
    @Column(nullable = false)
    Instant createdAt;
    
    Instant lastUpdatedAt;
}