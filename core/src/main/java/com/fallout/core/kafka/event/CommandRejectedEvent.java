package com.fallout.core.kafka.event;

import com.fallout.core.enums.ActionType;
import com.fallout.core.enums.KafkaEventType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CommandRejectedEvent extends GameEvent {

    @NotBlank
    private String nodeId;

    private String originalCommandId;

    private ActionType rejectedCommandType;

    @NotBlank
    private String rejectionReason;

    @Override
    public KafkaEventType getKafkaEventType() {
        return KafkaEventType.COMMAND_REJECTED;
    }
}
