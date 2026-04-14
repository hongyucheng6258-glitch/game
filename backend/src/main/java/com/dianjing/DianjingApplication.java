package com.dianjing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DianjingApplication {
    public static void main(String[] args) {
        SpringApplication.run(DianjingApplication.class, args);
    }
}
