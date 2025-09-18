package com.nathing.banthing.service;

import com.nathing.banthing.dto.response.MeetingDetailResponse;
import com.nathing.banthing.dto.response.MeetingParticipatedPageResponse;
import com.nathing.banthing.dto.response.MeetingSimpleResponse;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FindMeetingService 클래스는 모임(meeting)과 관련된 데이터 조회 기능을 제공하는 서비스 클래스입니다.
 * 해당 클래스는 주로 모임의 목록 조회와 특정 모임의 상세 정보를 제공하는 역할을 담당합니다.
 * <p>
 * 이 클래스는 Spring Framework의 서비스(Service) 계층에 속하며, 데이터의 읽기 전용성을 보장하기 위해
 *
 * @author - 고동현
 * @Transactional(readOnly = true) 어노테이션을 사용합니다. 생성자는 @RequiredArgsConstructor 어노테이션을 통해
 * 의존성을 명시적으로 주입받습니다.
 * <p>
 * 주요 기능:
 * 1. 전체 모임 목록 조회
 * 2. 특정 모임 ID를 기반으로 상세 정보 조회
 * 3. 주어진 사용자의 참여한 모임 목록을 페이징 처리하여 조회
 * <p>
 * 사용된 의존성:
 * - MeetingsRepository: 모임 데이터에 접근하기 위한 JPA 리포지토리 인터페이스입니다.
 * - MeetingSimpleResponse: 모임의 간략한 정보를 포함하는 DTO 클래스입니다.
 * - MeetingDetailResponse: 특정 모임의 상세 정보를 포함하는 DTO 클래스입니다.
 * - BusinessException 및 ErrorCode: 예외 처리 및 에러 코드 정의를 위한 클래스입니다.
 * <p>
 * 스레드 안전성:
 * 이 클래스는 상태를 가지지 않으므로 스레드에 대해 안전하게 사용할 수 있습니다.
 * @since - 2025-09-15
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FindMeetingService {
    private final MeetingsRepository meetingsRepository;
    private final UsersRepository usersRepository;

    /**
     * 전체 모임 목록 조회 (생성 시간 최신순으로 정렬)
     * @return 전체 모임의 핵심 정보 리스트
     */
    public List<MeetingSimpleResponse> searchMeetings(String keyword) {
        List<Meeting> meetings;

        // ️ 검색어가 비어 있지 않으면 키워드 검색을 수행합니다.
        if (keyword != null && !keyword.isBlank()) {
            // meetingsRepository에 이미 제목이나 설명에 키워드가 포함된 모임을 찾는 메서드가 있습니다.
            meetings = meetingsRepository.findByKeywordAndRecruiting(keyword);
        } else {
            // 검색어가 없으면 전체 모임을 최신순으로 조회합니다.
            meetings = meetingsRepository.findAllWithMartByOrderByCreatedAtDesc();
        }

        return meetings.stream()
                .map(MeetingSimpleResponse::new)
                .collect(Collectors.toList());
    }
    /**
     * 특정 모임 상세 조회
     *
     * @param meetingId 조회할 모임의 ID
     * @return 모임의 상세 정보
     */
    public MeetingDetailResponse findMeetingById(Long meetingId) {
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        return new MeetingDetailResponse(meeting);
    }

    /**
     * 주어진 사용자의 참여한 모임 목록을 페이징 처리하여 조회
     * 사용자 고유 식별자와 페이징 정보 기반으로 조회를 수행하며, 조회된 모임 정보를 DTO 형식으로 반환합니다.
     *
     * @param providerId 사용자의 고유 식별자(Provider ID)
     * @param pageable 페이징 처리를 위한 Pageable 객체
     * @return 사용자가 참여한 모임 목록 및 페이징 정보를 포함한 MeetingParticipatedPageResponse 객체
     *
     * @author 강관주
     * @since 2025-09-18
     */
    public MeetingParticipatedPageResponse getParticipatedMeetings(
            String providerId,
            Pageable pageable) {

        log.info("참여한 모임 목록 조회 서비스 메서드 - 페이징: {}", pageable);

        // 사용자 정보 조회
        User user = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 사용자가 참여한 모임을 페이징 정보와 함께 목록을 불러옴
        Page<Meeting> meetingPage = meetingsRepository.findApprovedMeetingsWithMartByUserId(user.getUserId(), pageable);

        // Meeting 엔티티 리스트를 MeetingDetailResponse 리스트로 변환
        List<MeetingDetailResponse> items = meetingPage.getContent().stream()
                .map(MeetingDetailResponse::new)
                .toList();


        return MeetingParticipatedPageResponse.builder()
                .content(items)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(meetingPage.getTotalElements())
                .build();
    }
}