package com.lostandfound.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.lostandfound.app.config")
public class LostandfoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(LostandfoundApplication.class, args);
    }
}
