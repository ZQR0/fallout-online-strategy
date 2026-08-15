package com.fallout.core.kafka.event;

import com.fallout.core.enums.KafkaEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PlayerResourcesUpdatedEvent extends GameEvent {

    private String nodeId;

    private int playerId;

    private int oldResources;
    private int newResources;

    private int resourceDelta;

    private String reason;

    @Override
    public KafkaEventType getKafkaEventType() {
        return KafkaEventType.PLAYER_RESOURCES_UPDATED;
    }
}
