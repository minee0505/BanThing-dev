package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingsRepository extends JpaRepository<Meeting, Long> {
}
