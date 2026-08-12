package com.fallout.core.kafka.command;

import com.fallout.core.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AttackCommand.class, name = "ATTACK"),
        @JsonSubTypes.Type(value = MoveCommand.class, name = "MOVE"),
        @JsonSubTypes.Type(value = ReinforceCommand.class, name = "REINFORCE")
})
public abstract class Command {

    @NotBlank
    private String commandId = UUID.randomUUID().toString();

    @NotBlank
    private String sessionId;

    @Positive
    private int playerId;

    @NotNull
    private Instant timestamp = Instant.now();


    /**
     * Каждая конкретная команда должна возвращать свой тип.
     * Используется для switch/case в обработчике.
     */
    public abstract ActionType getActionType();
}
