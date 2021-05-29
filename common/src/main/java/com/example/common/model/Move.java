package com.example.common.model;

import java.io.Serializable;

public enum Move implements Serializable {
    UP(0), DOWN(1), NO_CHANGE(2);

    private final int moveValue;

    Move(int moveValue) {
        this.moveValue = moveValue;
    }

    public int getMoveValue() {
        return moveValue;
    }
}
