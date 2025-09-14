package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.MeetingParticipant;
import com.nathing.banthing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingParticipantsRepository extends JpaRepository<MeetingParticipant, Long> {
    boolean existsByMeetingAndUserAndApplicationStatus(Meeting meeting, User giverUser, MeetingParticipant.ApplicationStatus applicationStatus);
}
