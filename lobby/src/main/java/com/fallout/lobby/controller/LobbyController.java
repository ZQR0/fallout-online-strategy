package com.fallout.lobby.controller;

import com.fallout.lobby.entity.SessionEntity;
import com.fallout.lobby.entity.SessionPlayerEntity;
import com.fallout.lobby.service.LobbyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/lobby")
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyService lobbyService;

    /**
     * Creates a new game session.
     * @return The created session with its unique join code.
     */
    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SessionEntity> createSession() {
        log.info("Request to create a new game session");
        return lobbyService.createSession();
    }

    /**
     * Joins an existing game session.
     * @param sessionId The UUID of the session.
     * @param player Player details (name, etc.).
     * @return The saved session player entity.
     */
    @PostMapping("/sessions/{sessionId}/join")
    public Mono<SessionPlayerEntity> joinSession(
            @PathVariable UUID sessionId,
            @RequestBody SessionPlayerEntity player) {
        log.info("Request to join session {}: player {}", sessionId, player.getDisplayName());
        return lobbyService.joinSession(sessionId, player);
    }

    /**
     * Retrieves basic information about a specific session.
     * @param sessionId The UUID of the session.
     * @return The session entity.
     */
    @GetMapping("/sessions/{sessionId}")
    public Mono<SessionEntity> getSession(@PathVariable UUID sessionId) {
        log.info("Request for session info: {}", sessionId);
        return lobbyService.getSession(sessionId);
    }

    /**
     * Lists all sessions currently in WAITING state.
     * @return A flux of waiting sessions.
     */
    @GetMapping("/sessions/waiting")
    public Flux<SessionEntity> listWaitingSessions() {
        log.info("Request for list of waiting sessions");
        return lobbyService.getWaitingSessions();
    }
}
