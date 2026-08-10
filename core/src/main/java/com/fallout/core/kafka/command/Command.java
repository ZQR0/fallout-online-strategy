package com.fallout.core.kafka.command;

import com.fallout.core.enums.ActionType;

public abstract class Command {
    protected ActionType actionType;

    protected Command(ActionType actionType) {
        this.actionType = actionType;
    }
}
