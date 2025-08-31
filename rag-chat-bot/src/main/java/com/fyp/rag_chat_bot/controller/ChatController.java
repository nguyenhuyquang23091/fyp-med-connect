package com.fyp.rag_chat_bot.controller;



import com.fyp.rag_chat_bot.dto.request.ChatRequest;
import com.fyp.rag_chat_bot.dto.response.ChatBotResponse;
import com.fyp.rag_chat_bot.services.RagService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/chat")
public class ChatController {
    RagService service;


    @PostMapping("/ai/generate")
    public ChatBotResponse chat(  @RequestBody ChatRequest chatRequest) {
        return service.chat(  chatRequest);
    }

    @DeleteMapping("ai/generate/{conversationId}")
    public void deleteConversation( @PathVariable String conversationId){
         service.deleteConversation(conversationId);
    }
}

