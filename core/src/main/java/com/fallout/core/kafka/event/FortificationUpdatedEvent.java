package com.fallout.core.kafka.event;

import com.fallout.core.enums.KafkaEventType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FortificationUpdatedEvent extends GameEvent {

    @NotBlank
    private String nodeId;

    private int oldFortification;
    private int newFortification;

    @Override
    public KafkaEventType getKafkaEventType() {
        return KafkaEventType.FORTIFICATION_UPDATED;
    }
}
