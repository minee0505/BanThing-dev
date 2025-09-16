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
 * 챗봇 메시지 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotMessageResponse {

    private String response;
    private List<MeetingSuggestionResponse> suggestedMeetings;
    private ChatbotConversation.IntentType intentType;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingSuggestionResponse {
        private Long meetingId;
        private String title;
        private String martName;
        private LocalDateTime meetingDate;
        private String suggestionReason;
        private Integer currentParticipants;
        private Integer maxParticipants;
    }
}