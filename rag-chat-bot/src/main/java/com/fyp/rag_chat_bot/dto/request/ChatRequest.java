package com.fyp.rag_chat_bot.dto;


import jakarta.annotation.Nullable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRequest {
    String conversationId;
    String message;
}
