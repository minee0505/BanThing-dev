package com.nathing.banthing.entity;

import lombok.Getter;

// 점수 이벤트를 나타내는 열거형(Enum)
@Getter
public enum ScoreEvent {
    MEETING_CREATED(2),
    MEETING_JOINED(2),
    NO_SHOW(-20),
    POSITIVE(5), // 긍정 피드백 점수 (예시)
    NEGATIVE(-5); // 부정 피드백 점수 (예시)


    private final int value;

    ScoreEvent(int value) {
        this.value = value;
    }

}
