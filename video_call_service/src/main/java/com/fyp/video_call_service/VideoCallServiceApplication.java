package com.fyp.video_call_service;

import com.corundumstudio.socketio.SocketIOServer;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
@EnableFeignClients
public class VideoCallServiceApplication implements CommandLineRunner {

	private final SocketIOServer socketIOServer;

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(VideoCallServiceApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		// All event listeners are registered in SocketIOConfiguration
		// Just start the server here
		socketIOServer.start();

		log.info("=".repeat(60));
		log.info("Socket.IO Server Started Successfully");
		log.info("=".repeat(60));
		log.info("  Listening on: {}:{}", socketIOServer.getConfiguration().getHostname(), socketIOServer.getConfiguration().getPort());
		log.info("  CORS Origin: {}", socketIOServer.getConfiguration().getOrigin());
		log.info("  WebRTC Events: 6 registered (joinRoom, ready, offer, answer, candidate, leaveRoom)");
		log.info("=".repeat(60));
		log.info("Server is ready to accept connections!");
	}


}
