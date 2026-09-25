package com.fallout.lobby.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lobby_event")
public class LobbyEventEntity {
    @Id
    private Long id;
    private UUID sessionId;
    private String eventType; // SESSION_CREATED, PLAYER_JOINED, PLAYER_LEFT, SESSION_STARTED, SESSION_CANCELED, SESSION_FINISHED
    private OffsetDateTime occurredAt;
    private Integer actorPlayerId;
    private String correlationId;
    private JsonNode payload;
}
