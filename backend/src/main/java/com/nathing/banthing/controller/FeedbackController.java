package com.nathing.banthing.controller;

import com.nathing.banthing.dto.enums.FeedbackSearchType;
import com.nathing.banthing.dto.request.FeedbackCreateRequest;
import com.nathing.banthing.dto.response.CommonResponse;
import com.nathing.banthing.dto.response.FeedbackResponse;
import com.nathing.banthing.dto.response.FeedbackScoreResponse;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.service.FeedbackScoreService;
import com.nathing.banthing.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * @author 송민재
 * @since 25. 9. 15.
 */
@RestController
@RequestMapping("api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final FeedbackScoreService feedbackScoreService;

    /**
     * 피드백 생성 API
     * @param request 피드백 생성 요청 DTO
     * @return 성공 시 HTTP 201 Created 응답
     */
    // POST /api/feedbacks
    @PostMapping
    public ResponseEntity<CommonResponse<FeedbackScoreResponse>> createFeedback(@RequestBody FeedbackCreateRequest request) {

        // DTO에서 giverId와 receiverId를 String으로 가져와서 사용
        String giverIdStr = request.getGiverId();
        String receiverIdStr = request.getReceiverId();

        if (giverIdStr == null || receiverIdStr == null) {
            // 이 경우 401 Unauthorized를 반환할 수 있으므로, CommonResponse로 감싸지 않는 것이 일반적입니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 변경된 서비스 메서드 시그니처에 맞춰 String 값을 전달
        User updatedReceiver = feedbackService.createFeedback(request, giverIdStr, receiverIdStr);

        // 업데이트된 유저의 점수를 담는 DTO를 생성합니다.
        FeedbackScoreResponse scoreResponse = new FeedbackScoreResponse(
                updatedReceiver.getUserId(),
                updatedReceiver.getTrustScore(),
                updatedReceiver.getTrustGrade()
        );

        // CommonResponse를 사용하여 성공 메시지와 점수 데이터를 함께 반환합니다.
        CommonResponse<FeedbackScoreResponse> response = CommonResponse.success(
                "피드백이 성공적으로 등록되었습니다.",
                scoreResponse
        );

        // HTTP 201 Created와 함께 응답 본문에 JSON 포함
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/feedbacks/users/{userId}?type=RECEIVED (?type=GIVEN)
    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<List<FeedbackResponse>>> getFeedbacksByUser(
            @PathVariable Long userId,
            @RequestParam(name = "type") FeedbackSearchType type) {

        // 통합된 서비스 메서드 호출
        List<FeedbackResponse> feedbacks = feedbackService.getFeedbacksByUserAndType(userId, type);

        // CommonResponse를 사용하여 응답 메시지와 데이터를 함께 반환
        String message = (type == FeedbackSearchType.RECEIVED) ? "받은 피드백 목록 조회 성공" : "준 피드백 목록 조회 성공";
        CommonResponse<List<FeedbackResponse>> response = CommonResponse.success(message, feedbacks);
        // HTTP 200 OK와 함께 응답 반환
        return ResponseEntity.ok(response); // 2. response 객체 반환
    }

    // 새로운 API: GET /api/feedbacks/users/{userId}/score
    @GetMapping("/users/{userId}/score")
    public ResponseEntity<CommonResponse<FeedbackScoreResponse>> getUserScore(@PathVariable Long userId) {
        FeedbackScoreResponse scoreResponse = feedbackScoreService.getUserScore(userId);

        CommonResponse<FeedbackScoreResponse> response = CommonResponse.success(
                "사용자 신뢰 점수 조회 성공",
                scoreResponse
        );

        return ResponseEntity.ok(response);
    }
}