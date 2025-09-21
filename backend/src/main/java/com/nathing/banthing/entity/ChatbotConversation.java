package com.nathing.banthing.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 챗봇 대화 엔티티
 * 사용자와 챗봇 간의 대화 내용을 저장하고 관리합니다.
 *
 * @author 김경민
 * @since 2025-09-22
 */
@Entity
@Table(name = "chatbot_conversations")
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotConversation {

    /** 대화 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long conversationId;

    /** 대화를 나눈 사용자 정보 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 사용자가 입력한 메시지 */
    @Column(name = "user_message", nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    /** 챗봇이 응답한 메시지 */
    @Column(name = "bot_response", nullable = false, columnDefinition = "TEXT")
    private String botResponse;

    /** 사용자 질문의 의도 분류 (기본값: GENERAL) */
    @Enumerated(EnumType.STRING)
    @Column(name = "intent_type")
    private IntentType intentType = IntentType.GENERAL;

    /** 대화 생성 시간 */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 대화 수정 시간 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 이 대화에서 추천된 모임 목록
     * - ToString 순환 참조 방지
     * - CascadeType.ALL로 대화 삭제 시 연관된 추천 정보도 함께 삭제
     * - orphanRemoval=true로 리스트에서 제거된 추천 정보도 DB에서 삭제
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatbotMeetingSuggestion> meetingSuggestions = new ArrayList<>();

    /**
     * 사용자 질문의 의도를 분류하는 열거형
     * - MEETING_SEARCH: 모임 검색/추천 요청
     * - SERVICE_GUIDE: 서비스 이용 방법 문의
     * - GENERAL: 일반적인 대화
     */
    public enum IntentType {
        MEETING_SEARCH, SERVICE_GUIDE, GENERAL
    }
}
