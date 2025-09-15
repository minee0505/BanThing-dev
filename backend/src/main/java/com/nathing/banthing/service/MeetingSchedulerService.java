package com.nathing.banthing.service;

import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.repository.MeetingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 이 클래스는 모임 관련 스케줄러 기능을 제공하며, 정해진 시간에 자동으로
 * 모임의 상태를 업데이트하는 로직을 포함하고 있습니다.
 *
 * 주요 기능:
 * 1. 모집 완료(FULL) 상태인 모임을 일정 시간 이후 진행 중(ONGOING) 상태로 변경.
 * 2. 진행 중(ONGOING) 상태인 모임을 24시간 이후 완료(COMPLETED) 상태로 자동 변경.
 *
 * 사용되는 Spring Framework 어노테이션:
 * - @Slf4j: 로깅 기능을 사용하기 위해 Lombok에서 제공하는 어노테이션.
 * - @Service: 스프링 컨텍스트에 서비스 계층의 컴포넌트로 등록.
 * - @RequiredArgsConstructor: Lombok에서 제공하며, final 필드에 대해 생성자를 자동으로 생성.
 * - @Scheduled: 스케줄 작업을 정의하는 Spring Framework 어노테이션.
 * - @Transactional: 데이터 저장소 작업에서 트랜잭션 처리를 보장.
 *
 * @author 고동현
 * @since - 2025-09-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingSchedulerService {

    private final MeetingsRepository meetingsRepository;

    /**
     * 매 분마다 실행되어, 모집 마감(FULL) 상태이고 시작 시간이 지난 모임을
     * 진행 중(ONGOING) 상태로 변경합니다.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void startScheduledMeetings() {
        log.info("Scheduler: Checking for meetings to start...");
        LocalDateTime now = LocalDateTime.now();
        List<Meeting> meetingsToStart = meetingsRepository
                .findByStatusAndMeetingDateBefore(Meeting.MeetingStatus.FULL, now);

        for (Meeting meeting : meetingsToStart) {
            try {
                meeting.startMeeting();
                log.info("Meeting {} status updated to ONGOING", meeting.getMeetingId());
            } catch (Exception e) {
                log.error("Failed to start meeting {}: {}", meeting.getMeetingId(), e.getMessage());
            }
        }
    }

    /**
     * 매일 자정에 실행되어, 진행 중(ONGOING)인 모임들 중
     * 모임 시작 시간으로부터 24시간이 지난 모임들을 자동으로 완료(COMPLETED) 처리합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoCompleteMeetings() {
        log.info("Scheduler: Checking for meetings to auto-complete...");
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<Meeting> meetingsToComplete = meetingsRepository
                .findByStatusAndMeetingDateBefore(Meeting.MeetingStatus.ONGOING, oneDayAgo);

        for (Meeting meeting : meetingsToComplete) {
            try {
                meeting.completeMeeting();
                log.info("Meeting {} auto-completed.", meeting.getMeetingId());
            } catch (Exception e) {
                log.error("Failed to auto-complete meeting {}: {}", meeting.getMeetingId(), e.getMessage());
            }
        }
    }
}