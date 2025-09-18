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
 * @since 2025-09-16
 * 챗봇 대화 기록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotConversationHistoryResponse {

    /** 대화 고유 식별자 */
    private Long conversationId;

    /** 사용자가 입력한 메시지 */
    private String userMessage;

    /** 챗봇이 응답한 메시지 */
    private String botResponse;

    /** 사용자 질문의 의도 분류 (모임검색, 서비스가이드, 일반) */
    private ChatbotConversation.IntentType intentType;

    /** 대화 생성 일시 */
    private LocalDateTime createdAt;

    /** 챗봇이 추천한 모임 목록 */
    private List<MeetingSuggestionInfo> suggestedMeetings;

    /*
     * 챗봇이 추천하는 모임 정보 DTO
     * DTO(Data Transfer Object) 내부에 정적 중첩 클래스(static nested class)를 사용
     * 즉, 챗봇의 응답이라는 큰 구조 안에서 '추천된 모임 목록'이라는 세부 항목으로 사용
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingSuggestionInfo {

        /** 추천된 모임의 고유 식별자 */
        private Long meetingId;

        /** 추천된 모임의 제목 */
        private String title;

        /** 챗봇이 해당 모임을 추천한 이유 */
        private String suggestionReason;

        /** 모임이 열리는 마트명 */
        private String martName;

        /** 모임 예정 일시 */
        private LocalDateTime meetingDate;

        /** 모임 현재 상태 (모집중, 마감, 진행중, 완료, 취소) */
        private String status;
    }
}