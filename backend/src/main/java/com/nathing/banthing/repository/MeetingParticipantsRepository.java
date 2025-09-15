package com.nathing.banthing.repository;

import com.nathing.banthing.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingParticipantsRepository extends JpaRepository<MeetingParticipant, Long> {
    Optional<Object> findByMeetingMeetingIdAndUserUserIdAndApplicationStatus(Long meetingId, Long currentUserId, MeetingParticipant.ApplicationStatus applicationStatus);
}
