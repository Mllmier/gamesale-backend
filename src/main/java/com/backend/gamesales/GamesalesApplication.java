package com.backend.gamesales;

import com.backend.gamesales.Config.ForgotPasswordProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ForgotPasswordProperties.class)
public class GamesalesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GamesalesApplication.class, args);
	}

}
