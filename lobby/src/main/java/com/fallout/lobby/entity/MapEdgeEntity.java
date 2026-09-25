package com.fallout.lobby.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("map_edge")
public class MapEdgeEntity {
    @Id
    private Long id;
    private UUID sourceNodeId;
    private UUID targetNodeId;
    private Double length;
    private Boolean isBidirectional;
    private Double travelModifier;
}
