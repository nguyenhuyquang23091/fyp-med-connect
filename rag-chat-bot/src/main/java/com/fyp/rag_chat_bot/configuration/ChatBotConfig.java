package com.fyp.rag_chat_bot.configuration;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore){
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.8d)
                        .topK(3)  // Reduced from 6 to 3 for more focused responses
                        .build())
                .build();
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
