package com.fallout.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Player view in the running game.
 *
 * <p>Mirrors the Redis hash {@code game:{sessionId}:player:{displayPlayerId}}
 * (see {@code core/src/main/java/com/fallout/core/CLAUDE.md} and
 * {@code fallout-wasteland-battle.md} §3.4): {@code name}, {@code resources},
 * {@code totalUnits}. The {@code displayPlayerId} is the stable 1..maxPlayers
 * id used in Kafka command/event messages; {@code sessionId} ties the player
 * to a game session.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Player {

    /** Session this player belongs to. */
    private UUID sessionId;

    /**
     * Stable, human-facing id within the session (1..maxPlayers).
     * The same value travels in every Kafka command/event for this player.
     */
    private int displayPlayerId;

    /** Display name shown in the UI. Source of truth in {@code session_player}. */
    private String name;

    /** Player colour (CSS-compatible string, e.g. {@code "#ff8800"}). */
    private String color;

    /** Current resources (derived from Redis hash field {@code resources}). */
    private int resources;

    /** Total units the player currently commands across all owned nodes. */
    private int totalUnits;
}
