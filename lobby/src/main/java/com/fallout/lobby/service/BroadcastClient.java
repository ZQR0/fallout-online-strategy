package com.fallout.lobby.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Client for communicating with the Broadcast service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastClient {

    private final WebClient.Builder webClientBuilder;
    private final String BROADCAST_URL = "http://localhost:8090";

    /**
     * Notifies the Broadcast service that a session has transitioned to the active state.
     */
    public Mono<Void> notifySessionStart(UUID sessionId) {
        log.info("Notifying Broadcast service about session start: {}", sessionId);

        return webClientBuilder.build()
                .post()
                .uri(BROADCAST_URL + "/internal/sessions/start")
                .bodyValue(Map.of("sessionId", sessionId.toString()))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Failed to notify Broadcast service: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty()); // Non-blocking, don't fail game start if notification fails
    }
}
