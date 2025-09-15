package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbacksRepository extends JpaRepository<Feedback, Long> {

    /**
     특정 미팅에서 사용자가 이미 피드백을 보냈는지 확인하는 메서드
     * @param meetingId 미팅ID
     * @param senderId 사용자ID
     * @return 참거짓 리턴
     * @author 송민재
     * @since 2025.09.12
     */
//    boolean existsByMeetingIdAndSenderId(Long meetingId, Long senderId);
}
