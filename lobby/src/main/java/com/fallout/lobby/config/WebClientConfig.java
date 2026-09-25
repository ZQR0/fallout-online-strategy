package com.fallout.lobby.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * Configures the WebClient.Builder bean.
     * Spring Boot doesn't always auto-configure the Builder bean by default
     * in all configurations, so we define it explicitly to avoid
     * UnsatisfiedDependencyException in BroadcastClient.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
