package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.ChatbotConversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 김경민
 * @since 2025-09-12
 * 챗봇 대화 기록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotConversationHistoryResponse {

    private Long conversationId;
    private String userMessage;
    private String botResponse;
    private ChatbotConversation.IntentType intentType;
    private LocalDateTime createdAt;
    private List<MeetingSuggestionInfo> suggestedMeetings;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingSuggestionInfo {
        private Long meetingId;
        private String title;
        private String suggestionReason;
    }
}