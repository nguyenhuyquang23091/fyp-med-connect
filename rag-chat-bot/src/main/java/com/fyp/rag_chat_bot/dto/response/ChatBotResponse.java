package com.fyp.rag_chat_bot.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatBotResponse {

    String role;
    String conversationId;
    String content;

}
