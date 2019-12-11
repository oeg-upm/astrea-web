package oeg.validation.astrea.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AstreaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AstreaServiceApplication.class, args);
	}

}
