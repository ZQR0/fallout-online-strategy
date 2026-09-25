package com.fallout.lobby.repository;

import com.fallout.lobby.entity.SessionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository for managing {@link SessionEntity} in PostgreSQL.
 * Provides reactive CRUD operations for game sessions.
 */
@Repository
public interface GameSessionRepository extends ReactiveCrudRepository<SessionEntity, UUID> {
    /**
     * Retrieves a game session by its unique join code.
     *
     * @param joinCode The unique 8-character code used by players to join.
     * @return A Mono emitting the session if found, or empty otherwise.
     */
    Mono<SessionEntity> findByJoinCode(String joinCode);
}
