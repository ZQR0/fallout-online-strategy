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
public class ReinforceCommand extends Command {

    @NotBlank
    private String nodeId;

    @Positive
    private int fortificationLevel; // на сколько увеличить укрепление

    @Override
    public ActionType getActionType() {
        return ActionType.REINFORCE;
    }
}
