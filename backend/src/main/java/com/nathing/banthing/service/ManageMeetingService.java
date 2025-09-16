package com.nathing.banthing.service;

import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.MeetingParticipant;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.MeetingParticipantsRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import jakarta.transaction.Transactional;
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

        // 승인 처리
        participant.approve();

        // 참가자 수 업데이트
        long approvedCount = meetingParticipantsRepository
                .countByMeetingAndApplicationStatus(meeting, MeetingParticipant.ApplicationStatus.APPROVED);
        meeting.setCurrentParticipants((int) approvedCount);

        // 최대 인원 도달 시 자동 모집 마감
        if (approvedCount >= meeting.getMaxParticipants()) {
            meeting.closeRecruitment();
        }
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