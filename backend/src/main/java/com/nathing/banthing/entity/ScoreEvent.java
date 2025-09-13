package com.nathing.banthing.entity;

// 점수 이벤트를 나타내는 열거형(Enum)
public enum ScoreEvent {
    MEETING_CREATED(2),
    MEETING_JOINED(2),
    NO_SHOW(-20);


    private final int value;

    ScoreEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
