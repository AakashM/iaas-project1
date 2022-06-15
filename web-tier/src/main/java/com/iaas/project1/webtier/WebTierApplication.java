package com.iaas.project1.webtier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class WebTierApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebTierApplication.class, args);
	}

}
