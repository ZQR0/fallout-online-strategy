package com.fallout.lobby.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("game_session")
public class SessionEntity {
    @Id
    private UUID id;
    private String joinCode;
    private String status; // CANCELED, WAITING, IN_PROGRESS, FINISHED
    private OffsetDateTime createdAt;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private Integer maxPlayers;
    private Integer currentTurn;
    private Integer winnerPlayerId;
    private String mapSeed;
}
