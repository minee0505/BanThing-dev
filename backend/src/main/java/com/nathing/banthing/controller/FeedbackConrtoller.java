package com.nathing.banthing.controller;

import com.nathing.banthing.dto.request.FeedbackCreateRequest;
import com.nathing.banthing.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Spring Security를 사용한다고 가정

/**
 * @author 송민재
 * @since 25. 9. 15.
 */
@RestController
@RequestMapping("api/feedback")
@RequiredArgsConstructor
public class FeedbackConrtoller {
    private final FeedbackService feedbackService;

    /**
     * 피드백 생성 API
     * @param request 피드백 생성 요청 DTO
     * @param giverId 피드백을 주는 사용자의 ID (현재 로그인 사용자)
     * @return 성공 시 HTTP 201 Created 응답
     */
    @PostMapping
    public ResponseEntity<Void> createFeedback(@RequestBody FeedbackCreateRequest request) {

        // DTO에서 giverId를 직접 가져와서 사용
        Long giverId = request.getGiverId();

        if (giverId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 서비스 레이어의 비즈니스 로직 호출
        feedbackService.createFeedback(request, giverId);

        // HTTP 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 이외에 Feedback 관련 API들을 추가할 수 있습니다.
    // 예: @GetMapping을 사용한 피드백 조회 등
}