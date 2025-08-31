package com.fyp.rag_chat_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
public class RagChatBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagChatBotApplication.class, args);
	}

}
