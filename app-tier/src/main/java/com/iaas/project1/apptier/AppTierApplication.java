package com.iaas.project1.apptier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class AppTierApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppTierApplication.class, args);
    }

}
