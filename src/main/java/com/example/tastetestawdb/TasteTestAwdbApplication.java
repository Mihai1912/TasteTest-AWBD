package com.example.tastetestawdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TasteTestAwdbApplication {

    public static void main(String[] args) {
        SpringApplication.run(TasteTestAwdbApplication.class, args);
    }
}