package com.nathing.banthing.controller;

import com.nathing.banthing.dto.request.ChatbotMessageRequest;
import com.nathing.banthing.dto.response.ChatbotMessageResponse;
import com.nathing.banthing.dto.response.ChatbotConversationHistoryResponse;
import com.nathing.banthing.dto.common.ApiResponse;
import com.nathing.banthing.entity.ChatbotConversation;
import com.nathing.banthing.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 김경민
 * @since 2025-09-12
 * AI 챗봇 관련 API 컨트롤러
 * - 로그인 없이도 기본적인 챗봇 기능 이용 가능
 * - 로그인한 사용자는 대화 기록 저장 및 개인화된 응답 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * 챗봇 메시지 전송 (로그인 선택적)
     * 로그인한 사용자: 대화 기록 저장 + 개인화된 응답
     * 비로그인 사용자: 기본 응답만 제공
     */
    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatbotMessageResponse>> sendMessage(
            @Valid @RequestBody ChatbotMessageRequest request,
            @AuthenticationPrincipal String subject,
            HttpServletRequest httpRequest) {

        try {
            log.info("=== 챗봇 메시지 요청 ===");
            log.info("로그인 여부: {}", subject != null);
            log.info("메시지: {}", request.getMessage());

            ChatbotMessageResponse response;

            if (subject != null) {
                // 로그인한 사용자 - 개인화된 응답 + 대화 기록 저장
                log.info("로그인 사용자 처리 시작 - providerId: {}", subject);
                try {
                    response = chatbotService.processAuthenticatedMessage(subject, request.getMessage());
                    log.info("로그인 사용자 응답 생성 완료");
                } catch (Exception authError) {
                    log.error("로그인 사용자 처리 중 오류 발생, 게스트 모드로 전환", authError);
                    response = chatbotService.processGuestMessage(request.getMessage());
                }
            } else {
                // 비로그인 사용자 - 기본 응답만 제공
                log.info("게스트 사용자 처리 시작");
                response = chatbotService.processGuestMessage(request.getMessage());
                log.info("게스트 사용자 응답 생성 완료");
            }

            ApiResponse<ChatbotMessageResponse> apiResponse = ApiResponse.success(
                    "챗봇 응답이 성공적으로 생성되었습니다.", response);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("=== 챗봇 메시지 처리 중 최종 오류 ===", e);
            log.error("오류 타입: {}", e.getClass().getSimpleName());
            log.error("오류 메시지: {}", e.getMessage());

            // 에러 상황에서도 사용자에게 도움이 되는 응답 제공
            ChatbotMessageResponse errorResponse = ChatbotMessageResponse.builder()
                    .response(subject != null ?
                            "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요." :
                            "현재 AI 서버에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요!")
                    .intentType(ChatbotConversation.IntentType.GENERAL)
                    .suggestedMeetings(new ArrayList<>())
                    .build();

            ApiResponse<ChatbotMessageResponse> apiResponse = ApiResponse.success(
                    "기본 응답을 제공합니다.", errorResponse);

            return ResponseEntity.ok(apiResponse);
        }
    }

    /**
     * 게스트 전용 챗봇 엔드포인트 (로그인 불필요) - 디버깅 강화버전
     */
    @PostMapping("/guest")
    public ResponseEntity<ApiResponse<ChatbotMessageResponse>> sendGuestMessage(
            @Valid @RequestBody ChatbotMessageRequest request) {

        try {
            log.info("=== 게스트 챗봇 요청 시작 ===");
            log.info("요청 메시지: {}", request.getMessage());

            // Google AI API 상태 확인
            boolean isHealthy = chatbotService.healthCheck();
            log.info("Google AI API 상태: {}", isHealthy ? "정상" : "연결 실패");

            ChatbotMessageResponse response = chatbotService.processGuestMessage(request.getMessage());
            log.info("응답 생성 완료: {}", response.getResponse().substring(0, Math.min(50, response.getResponse().length())));

            ApiResponse<ChatbotMessageResponse> apiResponse = ApiResponse.success(
                    "게스트 챗봇 응답이 생성되었습니다.", response);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("=== 게스트 챗봇 처리 중 오류 발생 ===", e);
            log.error("오류 메시지: {}", e.getMessage());
            log.error("오류 타입: {}", e.getClass().getSimpleName());

            ChatbotMessageResponse errorResponse = ChatbotMessageResponse.builder()
                    .response("현재 AI 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요. 🙏\n\n" +
                            "더 정확한 답변을 원하시면 회원가입 후 이용해보세요!\n\n" +
                            "디버그 정보: " + e.getMessage())
                    .intentType(ChatbotConversation.IntentType.GENERAL)
                    .build();

            ApiResponse<ChatbotMessageResponse> apiResponse = ApiResponse.success(
                    "기본 응답을 제공합니다.", errorResponse);

            return ResponseEntity.ok(apiResponse);
        }
    }

    /**
     * 대화 기록 조회 (로그인 필수)
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ChatbotConversationHistoryResponse>>> getChatHistory(
            @AuthenticationPrincipal String subject) {

        if (subject == null) {
            return ResponseEntity.ok(ApiResponse.success(
                    "로그인이 필요한 서비스입니다.", List.of()));
        }

        try {
            List<ChatbotConversationHistoryResponse> history = chatbotService.getChatHistory(subject);

            ApiResponse<List<ChatbotConversationHistoryResponse>> apiResponse = ApiResponse.success(
                    "대화 기록을 성공적으로 조회했습니다.", history);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("대화 기록 조회 중 오류 발생", e);

            ApiResponse<List<ChatbotConversationHistoryResponse>> apiResponse = ApiResponse.error(
                    "대화 기록 조회 중 오류가 발생했습니다.");

            return ResponseEntity.ok(apiResponse);
        }
    }

    /**
     * 서비스 상태 확인 (헬스체크)
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        try {
            boolean isHealthy = chatbotService.healthCheck();

            if (isHealthy) {
                return ResponseEntity.ok(ApiResponse.success(
                        "챗봇 서비스가 정상 작동 중입니다.", "HEALTHY"));
            } else {
                return ResponseEntity.ok(ApiResponse.error(
                        "챗봇 서비스에 일시적인 문제가 있습니다."));
            }

        } catch (Exception e) {
            log.error("헬스체크 중 오류 발생", e);
            return ResponseEntity.ok(ApiResponse.error(
                    "서비스 상태 확인 중 오류가 발생했습니다."));
        }
    }

    /**
     * 서비스 소개 (로그인 불필요)
     */
    @GetMapping("/intro")
    public ResponseEntity<ApiResponse<String>> getServiceIntro() {
        String intro = """
                안녕하세요! 반띵 AI 도우미입니다. 😊
                
                🛒 반띵은 대용량 상품을 여러 명이 함께 구매하고 소분하는 서비스예요.
                
                📍 서울 지역 8개 마트 (코스트코 4곳, 이마트 트레이더스 2곳, 롯데마트 2곳)에서 
                   다양한 소분 모임이 진행되고 있어요.
                
                💡 1-2인 가구도 대용량 상품을 합리적으로 구매할 수 있도록 도와드려요!
                
                궁금한 점이 있으시면 언제든 말씀해주세요!
                """;

        return ResponseEntity.ok(ApiResponse.success(
                "서비스 소개입니다.", intro));
    }
}