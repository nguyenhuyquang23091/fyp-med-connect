package com.fyp.notification_service;

import com.corundumstudio.socketio.SocketIOServer;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceApplication implements CommandLineRunner {

	private final SocketIOServer socketIOServer;

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		socketIOServer.start();
		log.info("Socket.IO server started successfully on port: {}", socketIOServer.getConfiguration().getPort());
	}
}
