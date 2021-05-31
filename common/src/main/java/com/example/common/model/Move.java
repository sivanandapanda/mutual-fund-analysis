package com.example.common.model;

import java.io.Serializable;
import java.util.stream.Stream;

public enum Move implements Serializable {
    UP(0), DOWN(1), NO_CHANGE(2);

    private final int moveValue;

    Move(int moveValue) {
        this.moveValue = moveValue;
    }

    public int getMoveValue() {
        return moveValue;
    }

    public static Move fromValue(int moveValue) {
        return Stream.of(Move.values()).filter(t -> t.moveValue == moveValue).findAny()
                .orElseThrow(() -> new RuntimeException("Unknown moveValue " + moveValue));
    }
}
