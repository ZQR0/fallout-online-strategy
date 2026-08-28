package com.fallout.core.kafka.command;

import com.fallout.core.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MoveCommand extends Command {

    @NotBlank
    private String sourceNodeId;

    @NotBlank
    private String targetNodeId;

    @Positive
    private int unitsCount;

    @Override
    public ActionType getActionType() {
        return ActionType.MOVE;
    }
}
