package com.fallout.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * Broadcast projection of a session participant.
 *
 * <p>Mirrors the {@code session_player} table (see resilient-squishing-newt.md):
 * an anonymous lobby slot identified by {@code displayPlayerId} (1..maxPlayers),
 * which is the same id that travels in every Kafka command/event message.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SessionPlayer {

    private UUID sessionId;
    private int displayPlayerId;
    private String displayName;
    private String color;
    private int seatOrder;
    private Instant joinedAt;
    private Instant leftAt;
}
