package br.com.doceterapia.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiDoceterapiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiDoceterapiaApplication.class, args);
	}

}
