// src/main/java/com/nathing/banthing/service/FeedbackScoreService.java
package com.nathing.banthing.service;

import com.nathing.banthing.dto.response.FeedbackScoreResponse;
import com.nathing.banthing.entity.TrustGrade;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackScoreService {
    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public FeedbackScoreResponse getUserScore(Long userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        int score = user.getTrustScore();
        TrustGrade grade = calculateTrustGrade(score); // 등급 계산 메서드 호출

        return new FeedbackScoreResponse(user.getUserId(), score, grade);
    }

    /**
     * 신뢰도 점수에 따라 등급을 계산하는 메서드
     */
    private TrustGrade calculateTrustGrade(int score) {
        if (score >= 500) {
            return TrustGrade.GOOD;
        } else if (score >= 101 && score <= 499) { // 점수 범위 명확화
            return TrustGrade.BASIC;
        } else {
            return TrustGrade.WARNING;
        }
    }
}