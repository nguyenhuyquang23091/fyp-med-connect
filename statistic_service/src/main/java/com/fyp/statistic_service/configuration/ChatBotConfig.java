package com.fyp.statistic_service.configuration;


import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatBotConfig {
    @Value("classpath:/prompt/prompt.st")
    private Resource systemResource;


    @Bean
    ChatClient chatClient (ChatClient.Builder builder){
        return builder
                .defaultSystem(systemResource).build();
    }

    @Bean
    ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository){
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)  // Reduced from 30 to 10 for more concise context
                .build();
    }
    @Bean
    MessageChatMemoryAdvisor chatMemoryAdvisor(ChatMemory chatMemory){
        return MessageChatMemoryAdvisor
                .builder(chatMemory).build();
    }


}
