package com.profile.vnpay;

import com.corundumstudio.socketio.SocketIOServer;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@RequiredArgsConstructor
public class VnpayApplication implements CommandLineRunner {

	private final SocketIOServer socketIOServer;

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});
		SpringApplication.run(VnpayApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		socketIOServer.start();
	}
}
