package com.nathing.banthing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackCreateRequest {
    private Long meetingId;
    private String giverId;
    private String  receiverId;
    private String feedbackType; // 피드백 상태 문자열
}
