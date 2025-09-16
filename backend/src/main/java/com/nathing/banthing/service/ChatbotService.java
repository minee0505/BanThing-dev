package com.nathing.banthing.service;

import com.nathing.banthing.dto.request.ChatbotMessageRequest;
import com.nathing.banthing.dto.response.ChatbotConversationHistoryResponse;
import com.nathing.banthing.dto.response.ChatbotMessageResponse;

import java.util.List;

public interface ChatbotService {

    // ===== 기존 메서드들 =====

    ChatbotMessageResponse processMessage(ChatbotMessageRequest request, Long userId);

    List<ChatbotConversationHistoryResponse> getConversationHistory(Long userId, int limit);

    void clearConversation(Long userId);

    boolean healthCheck();

    // ===== 새로운 메서드들 (로그인 선택적) =====

    /**
     * 로그인한 사용자의 메시지 처리 (providerId 기반)
     */
    ChatbotMessageResponse processAuthenticatedMessage(String providerId, String userMessage);

    /**
     * 게스트 사용자의 메시지 처리
     */
    ChatbotMessageResponse processGuestMessage(String userMessage);

    /**
     * 대화 기록 조회 (providerId 기반) - 기존 getConversationHistory와 유사하지만 providerId 사용
     */
    List<ChatbotConversationHistoryResponse> getChatHistory(String providerId);

    /**
     * 서비스 상태 확인 - 기존 healthCheck()와 동일하므로 별칭으로 처리
     */
    default boolean isServiceHealthy() {
        return healthCheck();
    }

}