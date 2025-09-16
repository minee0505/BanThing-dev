package com.nathing.banthing.service;

import com.nathing.banthing.dto.response.MeetingParticipantResponse;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.entity.MeetingParticipant;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.MeetingParticipantsRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


/**
 * JoinMeetingService 클래스는 모임 참가 신청 및 관련 로직을 처리하는 서비스 클래스입니다.
 * 이 클래스는 모임에 참가를 원하는 사용자와 모임 간의 상호작용을 관리하며,
 * 모임 호스트를 위한 참가 신청 관리 기능도 제공합니다.
 *
 * 주요 기능:
 * 1. 모임 참가 신청
 * 2. 참가 신청 목록 조회
 *
 * 클래스 내부의 모든 비즈니스 로직은 트랜잭션 관리 하에 실행됩니다.
 *
 * @author 고동현
 * @since - 2025-09-15
 */

@Service
@Transactional
@RequiredArgsConstructor

public class JoinMeetingService {

    private final MeetingsRepository meetingsRepository;
    private final UsersRepository usersRepository;
    private final MeetingParticipantsRepository meetingParticipantsRepository;

    /**
     * 모임 참가 신청 로직
     *
     * @param meetingId 신청할 모임 ID
     * @param userId 신청하는 사용자 ID
     */
    @Transactional
    public void joinMeeting(Long meetingId, Long userId) {
        // 1. 모임 존재 여부 및 모집 상태 확인
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        if (meeting.getStatus() != Meeting.MeetingStatus.RECRUITING) {
            throw new BusinessException(ErrorCode.MEETING_IS_NOT_RECRUITING);
        }

        // 2. 신청하는 사용자 정보 확인
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 중복 신청 방지
        boolean alreadyParticipated = meetingParticipantsRepository.existsByMeetingAndUser(meeting, user);
        if (alreadyParticipated) {
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATED);
        }

        // 4. 모임 호스트는 참가 신청할 수 없도록 방지
        if (meeting.getHostUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_JOIN_AS_HOST);
        }

        // 5. 모임 정원 체크
        long currentParticipantCount = meetingParticipantsRepository
                .countByMeetingAndApplicationStatus(meeting, MeetingParticipant.ApplicationStatus.APPROVED);
        
        if (currentParticipantCount >= meeting.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.MEETING_IS_FULL); // 새로운 에러 코드 필요
        }

        // 6. MeetingParticipant 엔티티 생성 (신청 대기 상태)
        MeetingParticipant newParticipant = MeetingParticipant.builder()
                .meeting(meeting)
                .user(user)
                .participantType(MeetingParticipant.ParticipantType.PARTICIPANT)
                .applicationStatus(MeetingParticipant.ApplicationStatus.PENDING)
                .build();

        meetingParticipantsRepository.save(newParticipant);
    }

    /**
     * 모임 참가 신청 목록 조회 로직 (호스트 전용)
     *
     * @param meetingId 신청 목록을 조회할 모임 ID
     * @param userId 요청을 보낸 사용자 ID (호스트인지 확인)
     * @return 참가 신청 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<MeetingParticipantResponse> getPendingParticipants(Long meetingId, Long userId) {

        // 1. 모임 존재 및 호스트 권한 확인
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        if (!meeting.getHostUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 2. 대기 상태인 참가자 목록 조회
        List<MeetingParticipant> participants = meetingParticipantsRepository
                .findByMeetingAndApplicationStatus(meeting, MeetingParticipant.ApplicationStatus.PENDING);

        // 3. DTO로 변환하여 반환
        return participants.stream()
                .map(MeetingParticipantResponse::new)
                .collect(Collectors.toList());
    }
}