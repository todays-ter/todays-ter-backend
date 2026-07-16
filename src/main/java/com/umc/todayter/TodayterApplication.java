package com.umc.todayter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TodayterApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodayterApplication.class, args);
	}

}
