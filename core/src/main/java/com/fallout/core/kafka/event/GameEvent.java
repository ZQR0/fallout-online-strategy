package com.fallout.core.kafka.event;

import com.fallout.core.enums.KafkaEventType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "event"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PointOwnerChangedEvent.class, name = "POINT_OWNER_CHANGED"),
        @JsonSubTypes.Type(value = GarrisonUpdatedEvent.class, name = "GARRISON_UPDATED"),
        @JsonSubTypes.Type(value = BattleReportEvent.class, name = "BATTLE_REPORT"),
        @JsonSubTypes.Type(value = GameFinishedEvent.class, name = "GAME_FINISHED"),
        @JsonSubTypes.Type(value = CommandRejectedEvent.class, name = "COMMAND_REJECTED"),
        @JsonSubTypes.Type(value = PlayerResourcesUpdatedEvent.class, name = "PLAYER_RESOURCES_UPDATED")
})
public abstract class GameEvent {

    @NotBlank
    private final String eventId = UUID.randomUUID().toString();

    @NotBlank
    private String sessionId;

    private int initiatorPlayerId; // кто вызвал событие (может быть 0 для системных)

    @NotNull
    private Instant timestamp = Instant.now();


    private int version = 1; // для будущей эволюции

    public abstract KafkaEventType getKafkaEventType();

}
