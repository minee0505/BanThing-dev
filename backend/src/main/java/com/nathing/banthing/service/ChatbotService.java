package com.nathing.banthing.service;

import com.nathing.banthing.dto.request.ChatbotMessageRequest;
import com.nathing.banthing.dto.response.ChatbotConversationHistoryResponse;
import com.nathing.banthing.dto.response.ChatbotMessageResponse;

import java.util.List;

public interface ChatbotService {

    ChatbotMessageResponse processMessage(ChatbotMessageRequest request, Long userId);

    /**
     * 특정 사용자의 최신 대화 기록 조회.
     */
    List<ChatbotConversationHistoryResponse> getConversationHistory(Long userId, int limit);

    /**
     * (V2) 사용자 질문의 의도 분석: 타입 안정성을 위해 Enum 기반.
     */
    IntentResult analyzeIntentV2(String userMessage);

    /**
     * (이전 버전) 문자열 반환 방식 — 호환성 유지를 위해 남겨두되 사용 자제.
     */
    @Deprecated
    String analyzeIntent(String userMessage);

    /**
     * 사용자의 대화 맥락(메모리) 초기화.
     */
    void clearConversation(Long userId);

    /**
     * 헬스 체크: 외부 GenAI API 접근 가능 여부 확인.
     */
    boolean healthCheck();

    /**
     * 의도 타입 (반띵 도메인 맞춤)
     */
    enum IntentType {
        FIND_GROUPS,          // 소분 모임 찾기
        CREATE_GROUP,         // 소분 모임 생성/호스트 흐름
        HYGIENE_GUIDE,        // 위생/준비물/소분 가이드
        HOW_TO_USE,           // 서비스 이용법/정책/결제/참여 방법
        FEEDBACK,             // 불편/피드백 전달
        SMALL_TALK,           // 잡담/일반 대화
        UNKNOWN               // 분류 불가
    }

    /**
     * 의도 분석 결과(타입 + 신뢰도)
     */
    record IntentResult(IntentType type, double confidence) {}
}