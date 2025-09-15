package com.nathing.banthing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", nullable = false)
    private ParticipantType participantType;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status")
    private ApplicationStatus applicationStatus = ApplicationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public enum ParticipantType {
        HOST, PARTICIPANT
    }

    public enum ApplicationStatus {
        PENDING, APPROVED, REJECTED
    }

    // 비즈니스 메서드
    public boolean isHost() {
        return participantType == ParticipantType.HOST;
    }

    public boolean isApproved() {
        return applicationStatus == ApplicationStatus.APPROVED;
    }


    // 참가 신청을 승인하는 비즈니스 메서드 추가
    public void approve() {
        // 이미 승인된 상태가 아닌, '대기중'일 때만 상태를 변경
        if (this.applicationStatus == ApplicationStatus.PENDING) {
            this.applicationStatus = ApplicationStatus.APPROVED;
        }
    }
}