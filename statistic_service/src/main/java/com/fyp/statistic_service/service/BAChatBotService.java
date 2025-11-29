package com.fyp.statistic_service.service;

import com.fyp.statistic_service.dto.request.ChatRequest;
import com.fyp.statistic_service.dto.response.ChatBotResponse;
import com.fyp.statistic_service.entity.SessionEntity;
import com.fyp.statistic_service.tool.BusinessAnalystTool;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BAChatBotService {

   ChatClient chatClient;
   BusinessAnalystTool businessAnalystTool;
   JdbcChatMemoryRepository chatMemoryRepository;
   ChatSessionService sessionService;



    public BAChatBotService(ChatClient chatClient,
                            MessageChatMemoryAdvisor chatMemoryAdvisor,
                            BusinessAnalystTool businessAnalystTool,
                            JdbcChatMemoryRepository chatMemoryRepository,
                            ChatSessionService sessionService){

        this.chatMemoryRepository = chatMemoryRepository;
        this.chatClient = chatClient.mutate().defaultAdvisors
                        (chatMemoryAdvisor)
                .build();

        this.businessAnalystTool = businessAnalystTool;
        this.sessionService =sessionService;

    }

    public ChatBotResponse chat(ChatRequest chatRequest){

        SessionEntity chatSession = sessionManagement(chatRequest.getConversationId());

        String conversationId = chatSession.getConversationId();
        log.info("Current conversationId {}" , chatRequest.getConversationId());
        log.info("Current userMessage {}" , chatRequest.getMessage());



        ChatResponse chatResponse
                = chatClient.prompt()
                .tools(businessAnalystTool)
                .user(userMessage -> userMessage.text(chatRequest.getMessage()))
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();


        String content = chatResponse.getResult().getOutput().getText();
        return ChatBotResponse.builder()
                .content(content)
                .role("assistant")
                .conversationId(conversationId)
                .build();
    }

    private SessionEntity sessionManagement(String conversationId){

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        return sessionService.findOrCreateSession(conversationId, userId);
    }







}
