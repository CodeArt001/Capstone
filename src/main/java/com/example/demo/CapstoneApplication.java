package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CapstoneApplication {

	public static void main(String[] args) {
		Dotenv.configure()
				.ignoreIfMissing()
				.load()
				.entries()
				.forEach(entry -> System.setProperty(entry.getKey(),
						System.getProperty(entry.getKey(), entry.getValue())));
		SpringApplication.run(CapstoneApplication.class, args);
	}

}
