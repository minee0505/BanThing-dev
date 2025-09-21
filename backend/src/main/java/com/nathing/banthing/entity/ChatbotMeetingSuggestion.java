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

/**
 * 챗봇 모임 추천 엔티티
 * 챗봇이 사용자에게 추천한 모임 정보를 저장하고 관리합니다.
 *
 * @author 김경민
 * @since 2025-09-22
 */
@Entity
@Table(name = "chatbot_meeting_suggestions")
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotMeetingSuggestion {

    /** 추천 정보 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Long suggestionId;

    /**
     * 이 추천이 이루어진 대화 정보 (지연 로딩)
     * - ToString 순환 참조 방지
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatbotConversation conversation;

    /**
     * 추천된 모임 정보 (지연 로딩)
     * - ToString 순환 참조 방지
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    /**
     * 모임 추천 이유
     * - 챗봇이 해당 모임을 추천한 구체적인 이유
     * - 사용자의 질문/관심사와 모임의 연관성 설명
     */
    @Column(name = "suggestion_reason", length = 200)
    private String suggestionReason;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}