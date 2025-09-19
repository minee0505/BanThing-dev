package com.nathing.banthing.service;

import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.MeetingParticipant;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.MeetingParticipantsRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ManageMeetingService 클래스는 모임 관리와 관련된 주요 비즈니스 로직을 제공하는 서비스 클래스입니다.
 * 모임에 대한 승인, 마감, 탈퇴, 완료 등의 작업을 담당하며, 데이터베이스와의 상호작용을 위해
 * MeetingsRepository, MeetingParticipantsRepository, UsersRepository를 의존합니다.
 *
 * @author 고동현
 * @since - 2025-09-15
 */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ManageMeetingService {

    private final MeetingsRepository meetingsRepository;
    private final MeetingParticipantsRepository meetingParticipantsRepository;
    private final UsersRepository usersRepository;




    /**
     * 참가 신청 수락
     */
    public void approveParticipant(Long meetingId, Long participantId, String hostProviderId) {
        log.info("===== approveParticipant 시작: meetingId={}, participantId={} =====", meetingId, participantId);

        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        log.info("모임 찾기 성공: {}", meeting.getTitle());

        User hostUser = usersRepository.findByProviderId(hostProviderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!meeting.getHostUser().equals(hostUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        log.info("호스트 권한 확인 완료");

        MeetingParticipant participant = meetingParticipantsRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));
        log.info("참가자 찾기 성공: {}, 현재 상태: {}", participant.getUser().getNickname(), participant.getApplicationStatus());

        participant.approve();
        log.info("참가자 상태 변경 완료: 새로운 상태: {}", participant.getApplicationStatus());

        int currentCount = meeting.getCurrentParticipants();
        int newParticipantCount = currentCount + 1;
        meeting.setCurrentParticipants(newParticipantCount);
        log.info("모임 현재 인원 업데이트: {} -> {}", currentCount, newParticipantCount);

        if (newParticipantCount >= meeting.getMaxParticipants()) {
            meeting.closeRecruitment();
            log.info("모임이 가득 차서 모집 마감 처리됨");
        }

        meetingParticipantsRepository.save(participant);
        meetingsRepository.save(meeting);
        log.info("===== 데이터베이스에 저장 시도... 트랜잭션 커밋 대기 =====");
    }

    /**
     * 모임 수동 모집 마감
     */
    public void closeRecruitment(Long meetingId, String hostProviderId) {
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        User hostUser = usersRepository.findByProviderId(hostProviderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!meeting.getHostUser().equals(hostUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        meeting.closeRecruitment();
    }


    /**
     * 참가 신청 거절 (상태를 REJECTED로 변경)
     */
    @Transactional
    public void rejectParticipant(Long meetingId, Long participantId, String hostProviderId) {
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        User hostUser = usersRepository.findByProviderId(hostProviderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 호스트 권한 확인
        if (!meeting.getHostUser().equals(hostUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        MeetingParticipant participant = meetingParticipantsRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

        // DB에서 삭제하는 대신, 상태를 'REJECTED'로 변경합니다.
        participant.reject();

    }

    /**
     * 모임 탈퇴 처리
     */
    public void leaveMeeting(Long meetingId, String providerId) {
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        User user = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MeetingParticipant participant = meetingParticipantsRepository
                .findByMeetingAndUser(meeting, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

        meetingParticipantsRepository.delete(participant);

        if (meeting.getStatus() == Meeting.MeetingStatus.FULL) {
            meeting.reopenRecruitment();
        }
    }


    /**
     * 모임 종료 처리
     */
    public void completeMeeting(Long meetingId, String hostProviderId) {
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        User hostUser = usersRepository.findByProviderId(hostProviderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!meeting.getHostUser().equals(hostUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        meeting.completeMeeting();
    }
}