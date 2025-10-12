package com.fyp.rag_chat_bot.services;




import com.fyp.rag_chat_bot.dto.request.ChatRequest;
import com.fyp.rag_chat_bot.dto.response.ApiResponse;
import com.fyp.rag_chat_bot.dto.response.ChatBotResponse;
import com.fyp.rag_chat_bot.dto.response.UserProfileResponse;
import com.fyp.rag_chat_bot.entity.SessionEntity;
import com.fyp.rag_chat_bot.repository.httpClient.ProfileClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RagService {
    ChatClient chatClient;
    ChatSessionService sessionService;
    ProfileClient profileClient;
    JdbcChatMemoryRepository chatMemoryRepository;

    public RagService(ChatClient chatClient, MessageChatMemoryAdvisor chatMemoryAdvisor
            , ChatSessionService chatSessionService, QuestionAnswerAdvisor questionAnswerAdvisor,
                      ProfileClient profileClient,
                      JdbcChatMemoryRepository chatMemoryRepository){
        this.chatMemoryRepository = chatMemoryRepository;

        this.chatClient = chatClient.mutate().defaultAdvisors
                        (chatMemoryAdvisor, questionAnswerAdvisor)
                .build();
        this.sessionService = chatSessionService;
        this.profileClient = profileClient;
    }

    public ChatBotResponse chat( ChatRequest chatRequest)  {

        String fullName;

        SessionEntity chatSession = sessionManagement(chatRequest.getConversationId());

        String conversationId = chatSession.getConversationId();

        log.info("Current conversationId {}" , chatRequest.getConversationId());
        log.info("Current userMessage {}" , chatRequest.getMessage());

       Optional<ApiResponse<UserProfileResponse>> profileResponse =  profileClient.getUserProfile();

       if (profileResponse.isPresent() && profileResponse.get().getResult() != null){
           var profile = profileResponse.get().getResult();
           String firstName = profile.getFirstName();
           String lastName = profile.getLastName();
           fullName = String.format("%s %s", firstName, lastName);
       }else {
           fullName = "Guest";
       }

        ChatResponse chatResponse =  chatClient.prompt()
                .system(sp-> sp.param("fullName", fullName))
                .user(userMessage -> userMessage.text(chatRequest.getMessage()))
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();

        return ChatBotResponse.builder()
                .content(content)
                .conversationId(conversationId)
                .build();

    }

    private SessionEntity sessionManagement(String conversationId){
        Optional<ApiResponse<UserProfileResponse>> profileResponse = profileClient.getUserProfile();
        
        String userId;
        if (profileResponse.isPresent() && profileResponse.get().getResult() != null) {
            // Authenticated user
            userId = profileResponse.get().getResult().getUserId();
            log.info("Session management for authenticated user: {}", userId);
        } else {
            // Guest user,  use a special guest identifier
            userId = "guest";
            log.info("Session management for guest user");
        }
        
        return sessionService.findOrCreateSession(conversationId, userId);
    }

    public void deleteConversation(String conversationId){
        chatMemoryRepository.deleteByConversationId(conversationId);
    }



}
