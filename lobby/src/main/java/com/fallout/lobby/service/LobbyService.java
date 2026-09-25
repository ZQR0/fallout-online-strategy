package com.fallout.lobby.service;

import com.fallout.lobby.entity.SessionEntity;
import com.fallout.lobby.entity.SessionPlayerEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service contract for managing the lobby lifecycle.
 * Handles session creation, player joining, and game start triggers.
 */
public interface LobbyService {
    /**
     * Creates a new game session in WAITING state.
     * @return The created session entity.
     */
    Mono<SessionEntity> createSession();

    /**
     * Adds a player to an existing session.
     * If the session reaches max capacity, it automatically triggers game start.
     * @param sessionId ID of the session to join.
     * @param player Player details to add.
     * @return The updated session player entity.
     */
    Mono<SessionPlayerEntity> joinSession(UUID sessionId, SessionPlayerEntity player);

    /**
     * Manually or automatically transitions a session from WAITING to IN_PROGRESS.
     * Initializes state in Redis and notifies the Broadcast service.
     * @param sessionId ID of the session to start.
     * @return Mono completing when the session is successfully started.
     */
    Mono<Void> startGame(UUID sessionId);


    /**
     * Retrieves a session by its ID.
     * @param sessionId ID of the session.
     * @return The session entity.
     */
    Mono<SessionEntity> getSession(UUID sessionId);

    /**
     * Retrieves all sessions currently waiting for players.
     * @return A Flux of sessions in WAITING status.
     */
    Flux<SessionEntity> getWaitingSessions();

    /**
     * Disconnects a player from their current session.
     * Marks the player as left in the database.
     * @param player The player entity to disconnect.
     * @return Mono emitting the updated player entity.
     */
    Mono<SessionPlayerEntity> disconnectFromSession(SessionPlayerEntity player);


}
