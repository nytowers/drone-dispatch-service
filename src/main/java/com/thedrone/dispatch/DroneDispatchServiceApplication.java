package com.thedrone.dispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DroneDispatchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DroneDispatchServiceApplication.class, args);
    }
}