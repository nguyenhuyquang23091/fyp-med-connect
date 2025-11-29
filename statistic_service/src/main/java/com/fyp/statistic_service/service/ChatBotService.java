package com.fyp.statistic_service.service;

import com.fyp.statistic_service.dto.request.ChatRequest;
import com.fyp.statistic_service.dto.response.ChatBotResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ChatBotService {

    private final ChatClient chatClient;

    public ChatBotService(ChatClient chatClient){
        this.chatClient = chatClient.mutate().build();
    }

    public ChatBotResponse chatOnce(ChatRequest chatRequest){
        ChatResponse chatResponse
                = chatClient.prompt()
                .user(userMessage -> userMessage.text(chatRequest.getMessage()))
                .call().chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        return ChatBotResponse.builder()
                .content(content)
                .role("assistant")
                .conversationId(null)
                .build();
    }





}
