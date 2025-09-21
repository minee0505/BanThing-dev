package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.ChatbotConversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 챗봇 메시지 응답 DTO
 * 사용자의 질문에 대한 챗봇의 응답을 담는 클래스입니다.
 *
 * 구조:
 * 1. 메인 응답: 챗봇의 텍스트 응답
 * 2. 추천 모임: 사용자 질문에 관련된 모임 목록
 * 3. 대화 정보: 의도 분류와 대화 ID
 *
 * @author 김경민
 * @since 2025-09-12
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotMessageResponse {

    /** 챗봇의 응답 메시지 (AI가 생성한 텍스트) */
    private String response;

    /**
     * 챗봇이 추천하는 모임 목록
     * - MEETING_SEARCH 의도일 때만 채워짐
     * - 최대 3개까지 추천
     */
    private List<MeetingSuggestionResponse> suggestedMeetings;

    /**
     * 사용자 질문의 의도 분류
     * MEETING_SEARCH: 모임 검색/추천 요청
     * SERVICE_GUIDE: 서비스 이용 방법 문의
     * GENERAL: 일반적인 대화
     */
    private ChatbotConversation.IntentType intentType;

    /**
     * 대화 고유 식별자
     * - 대화 추적 및 추천 모임 저장에 사용
     * - 프론트엔드에서 대화 상태 관리에 활용
     */
    private Long conversationId;

    /**
     * 챗봇이 추천하는 모임 정보를 담는 내부 클래스
     * 모임의 핵심 정보와 추천 이유를 포함합니다.
     */
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

        /**
         * 챗봇이 해당 모임을 추천한 이유
         * 예시: "사용자가 요청한 견과류 관련 모임입니다."
         */
        private String suggestionReason;

        /** 현재 참여 인원수 */
        private Integer currentParticipants;

        /** 최대 참여 가능 인원수 */
        private Integer maxParticipants;

        /**
         * 모임 현재 상태
         * RECRUITING: 모집 중
         * FULL: 모집 완료
         * ONGOING: 진행 중
         * COMPLETED: 완료됨
         * CANCELLED: 취소됨
         */
        private String status;

        /** 마트 상세 주소 (모임 장소) */
        private String martAddress;
    }
}