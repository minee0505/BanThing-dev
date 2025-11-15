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

    /** 챗봇의 응답 메시지 */
    private String response;

    /** 챗봇이 추천하는 모임 목록 */
    private List<MeetingSuggestionResponse> suggestedMeetings;

    /** 사용자 질문의 의도 분류 (모임검색, 서비스가이드, 일반) */
    private ChatbotConversation.IntentType intentType;

    /** 대화 고유 식별자 (대화 추적용) */
    private Long conversationId;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingSuggestionResponse {

        /** 추천된 모임의 고유 식별자 */
        private Long meetingId;

        /** 추천된 모임의 제목 */
        private String title;

        /** 모임이 열리는 마트명 */
        private String martName;

        /** 모임 예정 일시 */
        private LocalDateTime meetingDate;

        /** 챗봇이 해당 모임을 추천한 이유 */
        private String suggestionReason;

        /** 현재 참여 인원수 */
        private Integer currentParticipants;

        /** 최대 참여 가능 인원수 */
        private Integer maxParticipants;

        /** 모임 현재 상태 (RECRUITING, FULL, ONGOING, COMPLETED, CANCELLED) */
        private String status;

        /** 마트 상세 주소 */
        private String martAddress;
    }
}