package com.nathing.banthing.service;

import com.nathing.banthing.dto.response.MeetingParticipantResponse;
import com.nathing.banthing.dto.response.ParticipantListResponse;
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

import java.util.Collections;
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
     * @param userId    신청하는 사용자 ID
     */
    /**
     * 모임 참가 신청 로직
     *
     * @param meetingId 신청할 모임 ID
     * @param providerId 신청하는 사용자의 providerId
     */
    @Transactional
    public void joinMeeting(Long meetingId, String providerId) {
        // 1. 모임 존재 여부 및 모집 상태 확인
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        if (meeting.getStatus() != Meeting.MeetingStatus.RECRUITING) {
            throw new BusinessException(ErrorCode.MEETING_IS_NOT_RECRUITING);
        }

        // 2. 신청하는 사용자 정보 확인 (providerId로 조회)
        User user = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 중복 신청 방지 (거절된 사용자 재신청 방지)
        MeetingParticipant existingParticipant = meetingParticipantsRepository
                .findByMeetingAndUser(meeting, user)
                .orElse(null);

        if (existingParticipant != null) {
            if (existingParticipant.getApplicationStatus() == MeetingParticipant.ApplicationStatus.REJECTED) {
                throw new BusinessException(ErrorCode.REJECTED_PARTICIPANT_CANNOT_REAPPLY);
            }
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATED);
        }

        // 4. 모임 호스트는 참가 신청할 수 없도록 방지
        if (meeting.getHostUser().equals(user)) {
            throw new BusinessException(ErrorCode.CANNOT_JOIN_AS_HOST);
        }

        // 5. 모임 정원 체크
        long currentParticipantCount = meetingParticipantsRepository
                .countByMeetingAndApplicationStatus(meeting, MeetingParticipant.ApplicationStatus.APPROVED);

        if (currentParticipantCount >= meeting.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.MEETING_IS_FULL);
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
     * [수정] 모임의 참여자 목록을 조회합니다.
     * - 요청자가 호스트일 경우: 확정된 참여자와 대기중인 신청자 목록을 모두 반환합니다.
     * - 요청자가 일반 참여자일 경우: 확정된 참여자 목록만 반환합니다.
     *
     * @param meetingId 조회할 모임 ID
     * @param providerId    요청을 보낸 사용자 ID
     * @return 확정 및 대기 목록을 포함한 DTO
     */
    @Transactional(readOnly = true)
    public ParticipantListResponse getParticipants(Long meetingId, String providerId) {
        // 1. 모임과 사용자 정보 조회
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        User currentUser = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 요청자가 호스트인지 확인
        boolean isHost = meeting.getHostUser().equals(currentUser);

        // 3. '확정(APPROVED)' 상태인 참여자 목록은 공통으로 조회
        List<MeetingParticipant> approvedList = meetingParticipantsRepository
                .findByMeetingAndApplicationStatus(meeting, MeetingParticipant.ApplicationStatus.APPROVED);

        List<MeetingParticipant> pendingList;
        if (isHost) {
            // 4-1. 호스트일 경우에만 '대기(PENDING)' 상태인 참여자 목록을 조회
            pendingList = meetingParticipantsRepository
                    .findByMeetingAndApplicationStatus(meeting, MeetingParticipant.ApplicationStatus.PENDING);
        } else {
            // 4-2. 일반 참여자에게는 빈 대기 목록을 반환
            pendingList = Collections.emptyList();
        }

        // 5. 조회된 목록을 DTO로 변환
        List<MeetingParticipantResponse> approvedDto = approvedList.stream()
                .map(MeetingParticipantResponse::new)
                .collect(Collectors.toList());
        List<MeetingParticipantResponse> pendingDto = pendingList.stream()
                .map(MeetingParticipantResponse::new)
                .collect(Collectors.toList());

        return new ParticipantListResponse(approvedDto, pendingDto);
    }
}