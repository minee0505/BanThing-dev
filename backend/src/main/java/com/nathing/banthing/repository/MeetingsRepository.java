package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingsRepository extends JpaRepository<Meeting, Long> {


    // 특정 상태이면서 모임 시간이 지난 모임들 조회
    List<Meeting> findByStatusAndMeetingDateBefore(Meeting.MeetingStatus status, LocalDateTime dateTime);
}
