package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.Feedback;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private Long feedbackId;
    private GiverUserResponse giverUser;
    private String feedbackType;
    private LocalDateTime createdAt;

    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getFeedbackId(),
                GiverUserResponse.from(feedback.getGiverUser()),
                feedback.getFeedbackType().name(), // FeedbackType 열거형을 문자열로 변환
                feedback.getCreatedAt()
        );
    }
}