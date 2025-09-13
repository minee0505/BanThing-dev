package com.nathing.banthing.entity;

import jakarta.persistence.*;
import lombok.*;
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
 *
 * @author 강관주
 * @since 2025-09-13
 * - {@code provider}와 {@code providerId} 컬럼 조합에 대해 고유성(unique) 제약 조건인 {@code uk_provider_id}를 추가했습니다.
 * - {@link lombok.ToString}과 {@link lombok.EqualsAndHashCode} 애너테이션을 추가하여 객체의 상태를 문자열로 표현하고 비교할 수 있도록 했습니다.
 * - 객체 생성 편의성과 무결성 확보를 위해 빌더 패턴 기반 생성자를 추가했습니다.
 *
 * @author 송민재
 * @since 2025-09-13
 * - 점수를 업데이트, 반환하는 메서드를 추가했습니다.
 * @version 1.0.1
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_provider_id", columnNames = {"provider", "provider_id"})
        }
)
@Getter
@ToString(exclude = {"hostedMeetings", "participations", "conversations", "givenFeedbacks", "receivedFeedbacks"})
@EqualsAndHashCode(of = "userId")
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


    //

    /**
     * 점수를 업데이트하는 메서드
     * @param event 노쇼, 모임 생성/참가 등의 이벤트 처리
     * @auther 송민재
     * @since 2025-09-13
     */
    public void updateTrustScore(ScoreEvent event) {
        this.trustScore += event.getValue();
    }

    // 현재 누적 점수를 반환하는 메서드
    public int getTrustScore() {
        return this.trustScore;
    }


    /**
     * User 객체를 빌더 패턴을 사용하여 생성하는 생성자.
     * 사용자 정보를 초기화하며 입력값이 null인 경우 기본값을 설정합니다.
     * 객체를 생성할 때, 기본값이 설정된 항목을 패턴에 기재하지 않으면 자동으로 기본값이 설정됩니다.
     *
     * @param nickname 사용자의 닉네임
     * @param profileImageUrl 사용자의 프로필 이미지 URL
     * @param selfIntroduction 사용자의 자기소개
     * @param provider 사용자가 가입한 OAuth 제공자 (예: Google, Facebook 등)
     * @param providerId OAuth 제공자에서 부여한 사용자 ID
     * @param trustScore 사용자의 신뢰도 점수 (기본값: 300)
     * @param trustGrade 사용자의 신뢰도 등급 (기본값: BASIC)
     * @param noShowCount 사용자의 노쇼(미참석) 횟수 (기본값: 0)
     * @param agree 약관 동의 여부 (기본값: false)
     *
     * @since v1.0.1 (2025-09-13) - 초기 작성, @author 강관주
     */
    @Builder
    public User(
            String nickname
            , String profileImageUrl
            , String selfIntroduction
            , String provider
            , String providerId
            , Integer trustScore
            , TrustGrade trustGrade
            , Integer noShowCount
            , Boolean agree
    ) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.selfIntroduction = selfIntroduction;
        this.provider = provider;
        this.providerId = providerId;

        // 값이 null일 경우 기본값으로 대체
        this.trustScore = trustScore != null ? trustScore : 300;
        this.trustGrade = trustGrade != null ? trustGrade : TrustGrade.BASIC;
        this.noShowCount = noShowCount != null ? noShowCount : 0;
        this.agree = agree != null ? agree : false;
    }

    // 논리적 삭제 여부 확인
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
