package com.fallout.core.kafka.event;

import com.fallout.core.enums.KafkaEventType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GarrisonUpdatedEvent extends GameEvent {

    @NotBlank
    private String nodeId;

    private int oldGarrison;
    private int newGarrison;


    @Override
    public KafkaEventType getKafkaEventType() {
        return KafkaEventType.GARRISON_UPDATED;
    }
}
