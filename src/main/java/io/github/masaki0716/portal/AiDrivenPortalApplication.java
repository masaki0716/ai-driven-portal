package io.github.masaki0716.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiDrivenPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiDrivenPortalApplication.class, args);
	}

}
