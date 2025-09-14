package com.nathing.banthing.controller;

import com.nathing.banthing.dto.request.FeedbackCreateRequest;
import com.nathing.banthing.dto.response.FeedbackResponse;
import com.nathing.banthing.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Spring Security를 사용한다고 가정

import java.util.List;

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

    /**
     * 특정 사용자가 받은 피드백 리스트를 조회하는 API
     * @param userId 피드백을 조회할 사용자 ID
     * @return 피드백 리스트와 함께 HTTP 200 OK 응답
     */
    @GetMapping("/users/{userId}/received")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByUserId(@PathVariable Long userId) {
        // 서비스 메서드를 호출하여 피드백 리스트를 가져옴
        List<FeedbackResponse> feedbacks = feedbackService.getFeedbacksByReceiverId(userId);

        // HTTP 200 OK와 함께 피드백 리스트 반환
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/users/{userId}/given")
    public ResponseEntity<List<FeedbackResponse>> getGivenFeedbacksByUserId(@PathVariable Long userId) {
        // 서비스 메서드를 호출하여 피드백 리스트를 가져옴
        List<FeedbackResponse> feedbacks = feedbackService.getFeedbacksByGiverId(userId);

        // HTTP 200 OK와 함께 피드백 리스트 반환
        return ResponseEntity.ok(feedbacks);
    }
}