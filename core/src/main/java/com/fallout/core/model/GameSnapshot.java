package com.fallout.core.model;

import com.fallout.core.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Full read-only snapshot of a game session, returned by
 * {@code GET /lobby/session/{sessionId}/state}.
 *
 * <p>Composed of: session metadata, list of players, the static map
 * (nodes + edges), and the current runtime state of every node
 * (owner / garrison / fortification maps keyed by node id).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GameSnapshot {

    private UUID sessionId;
    private GameStatus status;
    private int currentTurn;

    private List<SessionPlayer> players;

    private List<MapNodeStatic> nodes;
    private List<Edge> edges;

    private Map<UUID, Integer> nodeOwners;
    private Map<UUID, Integer> nodeGarrisons;
    private Map<UUID, Integer> nodeFortification;
}
