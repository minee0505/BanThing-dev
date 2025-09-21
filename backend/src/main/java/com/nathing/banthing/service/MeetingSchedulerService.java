package com.nathing.banthing.service;

import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.repository.MeetingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 이 클래스는 모임 관련 스케줄러 기능을 제공하며, 정해진 시간에 자동으로
 * 모임의 상태를 업데이트하는 로직을 포함하고 있습니다.
 *
 * @author 고동현
 * @since - 2025-09-15
 * @version 1.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingSchedulerService {

    private final MeetingsRepository meetingsRepository;

    /**
     * [수정] 매 분마다 실행되어, 모집 중이거나 모집 마감 상태이고 시작 시간이 지난 모임을
     * '진행 중(ONGOING)' 상태로 변경합니다.
     *
     * 기존 로직에서는 'FULL' 상태의 모임만 처리하여, 인원이 다 차지 않은 모임이
     * 시작되지 않는 문제가 있었습니다. 이를 해결하기 위해 대상을 'RECRUITING' 상태까지 확장했습니다.
     *
     * 추가적으로, 참여자가 호스트 1명 뿐인 모임은 자동으로 'CANCELED' 처리하여
     * 불필요한 모임 진행을 방지하는 예외 처리를 추가했습니다.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void startScheduledMeetings() {
        log.info("Scheduler: Checking for meetings to start...");
        LocalDateTime now = LocalDateTime.now();

        // [수정] 조회 대상을 'RECRUITING', 'FULL' 두 가지 상태로 확장합니다.
        List<Meeting.MeetingStatus> targetStatuses = Arrays.asList(
                Meeting.MeetingStatus.RECRUITING,
                Meeting.MeetingStatus.FULL
        );

        // [수정] Repository의 findByStatusInAndMeetingDateBefore 메서드를 호출하여
        // 여러 상태를 한 번에 조회합니다.
        List<Meeting> meetingsToStart = meetingsRepository
                .findByStatusInAndMeetingDateBefore(targetStatuses, now);

        // 처리할 모임이 없으면 로그를 남기고 일찍 종료하여 불필요한 연산을 줄입니다.
        if (meetingsToStart.isEmpty()) {
            log.info("Scheduler: No meetings to start at this time.");
            return;
        }

        for (Meeting meeting : meetingsToStart) {
            try {
                // [추가] 예외 처리: '모집 중' 상태인데 참여자가 1명(호스트)뿐인 경우,
                // 모임을 진행하는 대신 'CANCELED' 상태로 변경하여 자동 취소 처리합니다.
                if (meeting.getParticipants().size() <= 1 && meeting.getStatus() == Meeting.MeetingStatus.RECRUITING) {
                    meeting.cancelMeeting(); // Meeting 엔티티에 cancelMeeting() 메서드 필요
                    log.warn("Meeting {} has been canceled due to no participants.", meeting.getMeetingId());
                } else {
                    // 그 외의 경우, 정상적으로 모임을 시작합니다.
                    meeting.startMeeting();
                    log.info("Meeting {} status updated to ONGOING", meeting.getMeetingId());
                }
            } catch (Exception e) {
                // 개별 모임 처리 중 에러가 발생하더라도 다른 모임에 영향을 주지 않도록 try-catch로 감쌉니다.
                log.error("Failed to process meeting {}: {}", meeting.getMeetingId(), e.getMessage());
            }
        }
    }

    /**
     * 매일 자정에 실행되어, 진행 중(ONGOING)인 모임들 중
     * 시작된 지 24시간이 지난 모임들을 자동으로 완료(COMPLETED) 처리합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoCompleteMeetings() {
        log.info("Scheduler: Checking for meetings to auto-complete...");
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<Meeting> meetingsToComplete = meetingsRepository
                .findByStatusAndMeetingDateBefore(Meeting.MeetingStatus.ONGOING, twentyFourHoursAgo);

        if (meetingsToComplete.isEmpty()) {
            log.info("Scheduler: No meetings to auto-complete at this time.");
            return;
        }

        for (Meeting meeting : meetingsToComplete) {
            try {
                meeting.completeMeeting();
                log.info("Meeting {} has been auto-completed.", meeting.getMeetingId());
            } catch (Exception e) {
                log.error("Failed to auto-complete meeting {}: {}", meeting.getMeetingId(), e.getMessage());
            }
        }
    }
}

