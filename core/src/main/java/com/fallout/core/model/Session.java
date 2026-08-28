package com.fallout.core.model;

import com.fallout.core.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * Broadcast projection of a game session.
 *
 * <p>Mirrors the {@code game_session} table columns (see resilient-squishing-newt.md):
 * a denormalised read-only view consumed by {@code broadcast} and clients over
 * WebSocket. State-of-truth for sessions lives in {@code lobby}'s PostgreSQL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Session {

    private UUID sessionId;
    private String joinCode;
    private GameStatus status;
    private int currentTurn;
    private Integer winnerPlayerId;
    private Instant startedAt;
    private Instant finishedAt;
    private int maxPlayers;
    private String mapSeed;
}
