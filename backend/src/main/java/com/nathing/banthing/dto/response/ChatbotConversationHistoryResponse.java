package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.ChatbotConversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 챗봇 대화 기록 응답 DTO
 * 사용자와 챗봇의 과거 대화 내역을 조회할 때 사용되는 클래스입니다.
 *
 * 특징:
 * - 대화는 영구적으로 저장되어 사용자가 나중에 다시 볼 수 있음
 * - 추천받은 모임 정보도 함께 저장되어 과거의 추천 이력 확인 가능
 * - 대화 의도 분류를 통해 사용자의 관심사 패턴 파악 가능
 *
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

    /**
     * 사용자가 입력한 메시지
     * 챗봇에게 던진 질문이나 요청 내용
     */
    private String userMessage;

    /**
     * 챗봇이 응답한 메시지
     * AI가 생성한 답변 텍스트
     */
    private String botResponse;

    /**
     * 사용자 질문의 의도 분류
     * - MEETING_SEARCH: 모임 검색/추천 요청 ("양재동 모임 있나요?")
     * - SERVICE_GUIDE: 서비스 이용 방법 문의 ("소분은 어떻게 하나요?")
     * - GENERAL: 일반적인 대화 ("안녕하세요")
     */
    private ChatbotConversation.IntentType intentType;

    /** 대화가 생성된 시점 */
    private LocalDateTime createdAt;

    /**
     * 이 대화에서 챗봇이 추천한 모임 목록
     * intentType이 MEETING_SEARCH인 경우에만 존재
     */
    private List<MeetingSuggestionInfo> suggestedMeetings;

    /**
     * 챗봇이 추천한 모임 정보 DTO
     * 대화 히스토리에서는 모임의 핵심 정보만 보여줍니다.
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

        /**
         * 챗봇이 이 모임을 추천한 이유
         * 사용자의 질문과 모임의 연관성 설명
         */
        private String suggestionReason;

        /** 모임이 열리는 마트명 */
        private String martName;

        /** 모임 예정 일시 */
        private LocalDateTime meetingDate;

        /**
         * 모임 상태
         * 과거 추천 당시의 모임 상태를 보여줌
         */
        private String status;
    }
}