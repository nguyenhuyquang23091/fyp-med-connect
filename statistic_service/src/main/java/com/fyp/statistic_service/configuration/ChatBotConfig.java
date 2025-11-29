package com.fyp.statistic_service.configuration;


import org.springframework.ai.chat.client.ChatClient;

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


}
