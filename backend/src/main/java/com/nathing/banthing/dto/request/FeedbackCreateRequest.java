package com.nathing.banthing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackCreateRequest {
    private Long meetingId;
    private Long giverId;
    private Long receiverId;
    private Boolean isPositive;
    private int scoreValue;


}
