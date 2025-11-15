package com.nathing.banthing.service;

import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.MeetingParticipant;
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
     *
     * [버그 수정] 모임 시작 조건이 전체 참여자 수가 아닌, '승인된' 참여자 수를 기준으로 하도록 수정했습니다.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void startScheduledMeetings() {
        log.info("스케줄러: 시작할 모임을 확인합니다...");
        LocalDateTime now = LocalDateTime.now();

        List<Meeting.MeetingStatus> targetStatuses = Arrays.asList(
                Meeting.MeetingStatus.RECRUITING,
                Meeting.MeetingStatus.FULL
        );

        List<Meeting> meetingsToStart = meetingsRepository
                .findByStatusInAndMeetingDateBefore(targetStatuses, now);

        if (meetingsToStart.isEmpty()) {
            log.info("스케줄러: 현재 시작할 모임이 없습니다.");
            return;
        }

        for (Meeting meeting : meetingsToStart) {
            try {
                // 1. 전체 참여자 목록에서 'APPROVED' 상태인 참여자만 필터링하여 수를 계산합니다.
                long approvedParticipantsCount = meeting.getParticipants().stream()
                        .filter(participant -> participant.getApplicationStatus() == MeetingParticipant.ApplicationStatus.APPROVED)
                        .count();

                // 2. '모집 중' 상태이면서 확정된 참여자가 1명(호스트) 이하인 경우, 모임을 'CANCELED'로 변경합니다.
                if (approvedParticipantsCount <= 1 && meeting.getStatus() == Meeting.MeetingStatus.RECRUITING) {
                    meeting.cancelMeeting(); // Meeting 엔티티의 cancelMeeting() 메서드 호출
                    log.warn("모임 ID {}가 확정된 참여자가 없어 취소되었습니다. (확정 인원: {})", meeting.getMeetingId(), approvedParticipantsCount);
                } else {
                    // 3. 그 외의 경우 (확정 참여자가 2명 이상), 정상적으로 모임을 시작합니다.
                    meeting.startMeeting(); // Meeting 엔티티의 startMeeting() 메서드 호출
                    log.info("모임 ID {}의 상태가 ONGOING(진행중)으로 업데이트되었습니다. (확정 인원: {})", meeting.getMeetingId(), approvedParticipantsCount);
                }

            } catch (Exception e) {
                log.error("모임 ID {} 처리 실패: {}", meeting.getMeetingId(), e.getMessage());
            }
        }
    }


    /**
     * 매일 자정에 실행되어, 진행 중(ONGOING)인 모임들 중
     * 시작된 지 24시간이 지난 모임들을 자동으로 완료(COMPLETED) 처리합니다.
     */
    @Scheduled(cron = "0 0 0 * * * ")
    @Transactional
    public void autoCompleteMeetings() {
        log.info("스케줄러: 자동 완료할 모임을 확인합니다...");
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<Meeting> meetingsToComplete = meetingsRepository
                .findByStatusAndMeetingDateBefore(Meeting.MeetingStatus.ONGOING, twentyFourHoursAgo);

        if (meetingsToComplete.isEmpty()) {
            log.info("스케줄러: 현재 자동 완료할 모임이 없습니다.");
            return;
        }

        for (Meeting meeting : meetingsToComplete) {
            try {
                meeting.completeMeeting();
                log.info("모임 ID {}가 자동으로 완료되었습니다.", meeting.getMeetingId());
            } catch (Exception e) {
                log.error("모임 ID {} 자동 완료 실패: {}", meeting.getMeetingId(), e.getMessage());
            }
        }
    }
}