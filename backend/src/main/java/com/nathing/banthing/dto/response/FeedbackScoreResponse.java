package com.nathing.banthing.dto.response;


import com.nathing.banthing.entity.TrustGrade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 @author user
 @since 25. 9. 15. */
// FeedbackScoreResponse.java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackScoreResponse {
    private Long userId;
    private Integer score;
    private TrustGrade trustGrade; // 추가: 등급 정보
}