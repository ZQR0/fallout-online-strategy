package com.fallout.lobby.repository;

import com.fallout.lobby.entity.SessionPlayerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

/**
 * Repository for managing {@link SessionPlayerEntity} in PostgreSQL.
 * Tracks players joined to specific game sessions.
 */
@Repository
public interface SessionPlayerRepository extends ReactiveCrudRepository<SessionPlayerEntity, Long> {
    /**
     * Retrieves all players currently associated with a given session.
     *
     * @param sessionId The unique identifier of the game session.
     * @return A Flux emitting players belonging to the session.
     */
    Flux<SessionPlayerEntity> findBySessionId(UUID sessionId);

    /**
     * Counts the number of players joined to a specific session.
     * Used to trigger the auto-start logic when max players are reached.
     *
     * @param sessionId The unique identifier of the game session.
     * @return A Mono emitting the total count of players.
     */
    long countBySessionId(UUID sessionId);
}
