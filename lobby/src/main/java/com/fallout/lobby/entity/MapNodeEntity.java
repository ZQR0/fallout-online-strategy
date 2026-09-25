package com.fallout.lobby.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("map_node")
public class MapNodeEntity {
    @Id
    private UUID id;
    private String name;
    private Double x;
    private Double y;
    private String type; // CAPITAL, BASE, CITY, RESOURCE, FARM, LAIR
    private Integer maxGarrison;
    private Integer baseFortification;
    private String description;
    private String iconUrl;
}
