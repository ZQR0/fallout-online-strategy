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
@Table("session_player_snapshot")
public class SessionPlayerSnapshotEntity {
    @Id
    private Long id;
    private UUID sessionId;
    private Integer displayPlayerId;
    private String displayName;
    private Integer finalUnits;
    private Integer finalResources;
    private Integer finalNodesControlled;
    private Integer rank;
    private OffsetDateTime snapshotAt;
}
