package com.tek.ping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author chengliangpu
 * @date 2024/11/10
 */
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClientWithPong() {
        return WebClient.create("http://localhost:8081/pong?say=Hello");
    }
}