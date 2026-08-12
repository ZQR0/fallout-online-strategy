package com.fallout.core.kafka.command;

import lombok.*;
import com.fallout.core.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
public class AttackCommand extends Command {

    public AttackCommand() {}

    @NotBlank
    private String sourceNodeId;

    @NotBlank
    private String targetNodeId;

    @Positive
    private int unitsCount;

    @Override
    public ActionType getActionType() {
        return ActionType.ATTACK;
    }
}
