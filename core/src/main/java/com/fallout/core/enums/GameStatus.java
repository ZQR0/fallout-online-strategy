package com.fallout.core.enums;

/**
 * There are statuses of game processing
 * */
public enum GameStatus {
    CANCELED,
    WAITING, // means that lobby is not full, need more players to start
    IN_PROGRESS,
    FINISHED;
}
