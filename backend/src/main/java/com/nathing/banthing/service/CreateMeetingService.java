package com.nathing.banthing.service;

import com.nathing.banthing.dto.request.MeetingCreateRequest;
import com.nathing.banthing.entity.Mart;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.MeetingParticipant;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.MartsRepository;
import com.nathing.banthing.repository.MeetingParticipantsRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j


public class CreateMeetingService {

    /**
     * MartsRepository를 통해 마트 관련 데이터에 접근하고 조작하는 저장소 객체입니다.
     * 이 객체는 데이터베이스에서 마트 정보를 조회하거나 저장하는 데 사용됩니다.
     *
     * 주요 역할:
     * 1. 주어진 마트 ID로 특정 마트를 조회합니다.
     * 2. 마트 데이터의 CRUD 작업을 처리합니다.
     *
     * 이 객체는 CreateMeetingService 클래스에서 마트를 조회하거나
     * 필요한 비즈니스 로직 실행 시 사용됩니다.
     * 예를 들어, 새로운 모임을 생성할 때 요청으로 전달된 마트 ID를 기반으로 마트를 조회합니다.
     * @author - 고동현
     * @Since - 2025-09-12
     */

    private final MartsRepository martsRepository;
    private final UsersRepository usersRepository;
    private final MeetingsRepository meetingsRepository;
    private final MeetingParticipantsRepository meetingParticipantsRepository;

    /**
     * 모임 생성 비즈니스 로직
     * @param request 모임 생성에 필요한 데이터 DTO
     * @param providerId 현재 로그인하여 모임을 생성하는 사용자 ID
     * @return 생성된 Meeting 엔티티 정보
     */
    public Meeting createMeeting(MeetingCreateRequest request, String providerId) {

        // userId가 null인지 검증
        if (providerId == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_NOT_FOUND);
        }

        // 사용자 정보 조회
        User hostUser = usersRepository.findByProviderId(providerId).orElseThrow(
                ()->new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 마트 정보 조회
        Mart mart = martsRepository.findById(request.getMartId()).orElseThrow(
                () -> new BusinessException(ErrorCode.MART_NOT_FOUND)
        );

        // 3. Meeting 엔티티 생성
        // Builder 패턴을 사용해 DTO와 조회된 엔티티 정보로 새로운 Meeting 객체를 만듭니다.
        // currentParticipants와 status는 서버에서 직접 값을 지정해 안정성을 높입니다.
        Meeting newMeeting = Meeting.builder()
                .hostUser(hostUser)
                .mart(mart)
                .title(request.getTitle())
                .description(request.getDescription())
                .meetingDate(request.getMeetingDate())
                .maxParticipants(5)
                .thumbnailImageUrl(request.getThumbnailImageUrl())
                .currentParticipants(1) // 모임 생성 시, 참여 인원은 항상 1명(호스트)으로 시작
                .status(Meeting.MeetingStatus.RECRUITING) // 모임 상태는 '모집중'으로 시작
                .build();

        // 4. 생성된 모임을 DB에 저장
        Meeting savedMeeting = meetingsRepository.save(newMeeting);

        // 5. 모임 생성자를 HOST로 참여자 목록에 추가
        MeetingParticipant hostParticipant = MeetingParticipant.builder()
                .meeting(savedMeeting)
                .user(hostUser)
                .participantType(MeetingParticipant.ParticipantType.HOST)
                .applicationStatus(MeetingParticipant.ApplicationStatus.APPROVED)
                .build();

        meetingParticipantsRepository.save(hostParticipant);

        // 로그를 남겨서 서버 동작을 쉽게 추적할 수 있도록 합니다.
        log.info("새로운 모임이 생성되었습니다. meetingId: {}, hostId: {}", savedMeeting.getMeetingId(), hostUser.getUserId());

        // 6. 저장된 Meeting 엔티티 반환
        return savedMeeting;

    }




}
