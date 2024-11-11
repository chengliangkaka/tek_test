package com.tek.ping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableScheduling
public class PingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PingServiceApplication.class, args);
        // skip the error that not handled
        Hooks.onErrorDropped(error -> {

        });
    }
}
