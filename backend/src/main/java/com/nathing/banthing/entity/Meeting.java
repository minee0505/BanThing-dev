package com.nathing.banthing.entity;

import com.nathing.banthing.dto.request.MeetingUpdateRequest;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meetings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE meetings SET deleted_at = NOW() WHERE meeting_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mart_id", nullable = false)
    private Mart mart;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "meeting_date", nullable = false)
    private LocalDateTime meetingDate;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants = 5;

    @Column(name = "current_participants")
    private Integer currentParticipants = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MeetingStatus status = MeetingStatus.RECRUITING;

    @Column(name = "thumbnail_image_url", length = 500)
    private String thumbnailImageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 연관관계 매핑
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatbotMeetingSuggestion> suggestions = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    // 송민재 작성, comments테이블과 연관관계
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public enum MeetingStatus {
        RECRUITING, FULL, ONGOING, COMPLETED, CANCELLED
    }

    // 논리적 삭제 여부 확인
    public boolean isDeleted() {
        return deletedAt != null;
    }

    // 비즈니스 메서드
    public boolean isRecruiting() {
        return status == MeetingStatus.RECRUITING && currentParticipants < maxParticipants;
    }

    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    //  업데이트 로직을 위한 메서드 추가
    public void update(MeetingUpdateRequest request, Mart newMart) {
        this.mart = newMart;
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.meetingDate = request.getMeetingDate();
        this.thumbnailImageUrl = request.getThumbnailImageUrl();
    }

    // '모집 완료'는 상태를 FULL로 변경
    public void closeRecruitment() {
        if (this.status != MeetingStatus.RECRUITING) {
            throw new BusinessException(ErrorCode.INVALID_MEETING_STATUS);
        }
        this.status = MeetingStatus.FULL;
    }

    // '모집 재개'는 상태를 RECRUITING으로 변경
    public void reopenRecruitment() {
        if (this.status != MeetingStatus.FULL) {
            throw new BusinessException(ErrorCode.INVALID_MEETING_STATUS);
        }
        this.status = MeetingStatus.RECRUITING;
    }

    // '모임 시작'은 FULL 상태에서 ONGOING으로 변경
    public void startMeeting() {
        if (this.status != MeetingStatus.FULL) {
            throw new BusinessException(ErrorCode.INVALID_MEETING_STATUS);
        }
        this.status = MeetingStatus.ONGOING;
    }

    // '모임 종료'는 ONGOING 상태에서 COMPLETED로 변경 (기존 로직 유지)
    public void completeMeeting() {
        if (this.status != MeetingStatus.ONGOING) {
            throw new BusinessException(ErrorCode.INVALID_MEETING_STATUS);
        }
        this.status = MeetingStatus.COMPLETED;
    }

    }

