package com.nathing.banthing.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 정보를 담는 엔티티 클래스.
 * 소분 소모임 플랫폼의 핵심 주체로서 다양한 활동에 참여합니다.
 *
 * @author 김경민
 * @since 2025-09-10
 * @version 1.0.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "self_introduction", length = 255)
    private String selfIntroduction;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(name = "trust_score", nullable = false)
    private Integer trustScore = 300;

    @Enumerated(EnumType.STRING)
    @Column(name = "trust_grade", nullable = false)
    private TrustGrade trustGrade = TrustGrade.BASIC;

    @Column(name = "no_show_count")
    private Integer noShowCount = 0;

    @Column(name = "agree", nullable = false)
    private Boolean agree = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 연관관계 매핑
    @OneToMany(mappedBy = "hostUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meeting> hostedMeetings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatbotConversation> conversations = new ArrayList<>();

    @OneToMany(mappedBy = "giverUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> givenFeedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "receiverUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> receivedFeedbacks = new ArrayList<>();

    public enum TrustGrade {
        WARNING, BASIC, GOOD
    }

    // 논리적 삭제 여부 확인
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
