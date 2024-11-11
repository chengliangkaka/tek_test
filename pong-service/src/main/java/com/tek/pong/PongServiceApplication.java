package com.tek.pong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class PongServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PongServiceApplication.class, args);
        // skip the error that not handled
        Hooks.onErrorDropped(error -> {

        });
    }
}
