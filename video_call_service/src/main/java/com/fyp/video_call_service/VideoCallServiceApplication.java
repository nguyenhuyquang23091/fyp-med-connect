package com.fyp.video_call_service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fyp.video_call_service.service.SocketEventHandler;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;




@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class VideoCallServiceApplication implements CommandLineRunner {

	private final SocketIOServer socketIOServer;
	private final SocketEventHandler socketEventHandler;

	public static void main(String[] args) {
		SpringApplication.run(VideoCallServiceApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		// Register the event handler with the Socket.IO server
		socketIOServer.addListeners(socketEventHandler);
		log.info("SocketEventHandler registered with SocketIOServer");

		// Start the server
		socketIOServer.start();
		log.info("Socket.IO server started successfully");
		log.info("  Listening on: {}:{}", socketIOServer.getConfiguration().getHostname(), socketIOServer.getConfiguration().getPort());
		log.info("  CORS Origin: {}", socketIOServer.getConfiguration().getOrigin());
		log.info("Server is ready to accept connections!");
	}

	@PreDestroy
	public void onShutdown() {
		if (socketIOServer != null) {
			log.info("Shutting down Socket.IO server...");
			socketIOServer.stop();
			log.info("Socket.IO server stopped successfully");
		}
	}

}
