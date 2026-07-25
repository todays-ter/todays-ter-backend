package com.umc.todayter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableFeignClients
@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class TodayterApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodayterApplication.class, args);
	}

}
