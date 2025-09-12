package com.nathing.banthing.repository;

import com.nathing.banthing.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingParticipantsRepository extends JpaRepository<MeetingParticipant, Long> {
}
