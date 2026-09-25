package com.fallout.lobby.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("session_map")
public class SessionMapEntity {
    @Id
    private Long id;
    private UUID sessionId;
    private String name;
    private Integer version;
    private String notes;
}
