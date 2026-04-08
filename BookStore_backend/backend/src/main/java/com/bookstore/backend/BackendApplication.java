package com.bookstore.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class BackendApplication {


	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

		SpringApplication.run(BackendApplication.class, args);
	}
	@EventListener(ApplicationReadyEvent.class)
	public void openSwagger() {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(
						new URI("http://localhost:8080/swagger-ui/index.html"));
			} else {
				System.out.println("⚠️ Desktop is not supported. Open manually: http://localhost:8080/swagger-ui/index.html");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
