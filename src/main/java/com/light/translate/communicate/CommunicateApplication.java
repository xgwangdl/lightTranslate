package com.light.translate.communicate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommunicateApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunicateApplication.class, args);
	}

}
