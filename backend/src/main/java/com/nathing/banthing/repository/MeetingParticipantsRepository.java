package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.MeetingParticipant;
import com.nathing.banthing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import java.util.Optional;

public interface MeetingParticipantsRepository extends JpaRepository<MeetingParticipant, Long> {

    boolean existsByMeetingAndUserAndApplicationStatus(Meeting meeting, User giverUser, MeetingParticipant.ApplicationStatus applicationStatus);

    // 특정 모임과 사용자로 참가자 존재 여부 확인 (중복 신청 방지)
    boolean existsByMeetingAndUser(Meeting meeting, User user);

    // 특정 모임의 특정 상태에 해당하는 참가자 목록 조회
    List<MeetingParticipant> findByMeetingAndApplicationStatus(Meeting meeting, MeetingParticipant.ApplicationStatus applicationStatus);

    // 특정 모임의 특정 사용자를 조회
    Optional<MeetingParticipant> findByMeetingAndUser(Meeting meeting, User user);


    long countByMeetingAndApplicationStatus(Meeting meeting, MeetingParticipant.ApplicationStatus status);

    Optional<Object> findByMeetingMeetingIdAndUserUserIdAndApplicationStatus(Long meetingId, Long currentUserId, MeetingParticipant.ApplicationStatus applicationStatus);
}
