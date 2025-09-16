package com.nathing.banthing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"giver_user_id", "receiver_user_id", "meeting_id"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giver_user_id", nullable = false)
    private User giverUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id", nullable = false)
    private User receiverUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

//    @Column(name = "is_positive", nullable = false)
//    private Boolean isPositive;

    // isPositive 대신 feedbackType으로 변경
    @Enumerated(EnumType.STRING) // 열거형을 DB에 문자열로 저장
    @Column(name = "feedback_type", nullable = false)
    private FeedbackType feedbackType; // **타입을 FeedbackType으로 변경**

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}