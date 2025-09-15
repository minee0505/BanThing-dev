package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Feedback;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbacksRepository extends JpaRepository<Feedback, Long> {

    // 피드백을 주는 사람, 받는 사람, 미팅 ID로 피드백이 이미 존재하는지 확인
    boolean existsByMeetingAndGiverUserAndReceiverUser(Meeting meeting, User giverUser, User receiverUser);

    List<Feedback> findByReceiverUser_UserId(Long receiverId);

    List<Feedback> findByGiverUser_UserId(Long giverId);
}
