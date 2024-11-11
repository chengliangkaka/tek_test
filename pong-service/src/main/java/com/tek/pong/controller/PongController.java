package com.tek.pong.controller;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pong")
public class PongController {
    private final RateLimiter rateLimiter = RateLimiter.create(1.0); // 1 request per second
    private final Logger logger = LoggerFactory.getLogger(PongController.class);

    @GetMapping
    public Mono<ResponseEntity<String>> getPongResponse(@RequestParam("say") String say) {
        if (rateLimiter.tryAcquire()) {
            logger.info("Receive the ping service request: {}", say);
            return Mono.just(ResponseEntity.ok("World"));
        } else {
            logger.error("Receive the ping service too many requests");
            return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
        }
    }
}