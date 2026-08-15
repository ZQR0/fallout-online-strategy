package com.fallout.core.kafka.event;

import com.fallout.core.enums.KafkaEventType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PointOwnerChangedEvent extends GameEvent {

    @NotBlank
    private String nodeId;

    private Integer oldOwnerId;
    private Integer newOwnerId;

    private int newGarrisonCount;

    @Override
    public KafkaEventType getKafkaEventType() {
        return KafkaEventType.POINT_OWNER_CHANGED;
    }
}
