package com.nathing.banthing.service;
import com.nathing.banthing.dto.request.MeetingUpdateRequest;
import com.nathing.banthing.entity.Mart;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.MartsRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * UpdateMeetingService 클래스는 모임 수정 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 주로 데이터베이스에 저장된 특정 모임 정보를 업데이트하거나 유효성 검증을 수행합니다.
 *
 * 이 클래스는 @Service 및 @Transactional 애너테이션을 통해 스프링 빈으로 관리되며,
 * 트랜잭션 범위 내에서 작동하여 데이터 일관성을 유지합니다. 또한 @Slf4j를 사용하여 로깅 기능을 제공합니다.
 *
 * 주요 기능:
 * - 특정 모임 정보를 ID를 통해 조회하고, 해당 모임 정보가 없을 경우 예외를 발생시킵니다.
 * - 요청한 사용자가 모임의 호스트인지 확인하여 권한을 검증합니다.
 * - 요청 데이터(예: 수정할 마트 ID)를 기반으로 관련 정보를 조회하며,
 *   관련 데이터(마트)가 존재하지 않을 경우 예외를 발생시킵니다.
 * - 모임 수정 요청 데이터를 기반으로 모임 정보를 업데이트합니다.
 * - 모임 업데이트 후 관련 정보를 로깅합니다.
 *
 * 메서드:
 * - updateMeeting(Long meetingId, MeetingUpdateRequest request, Long userId):
 *   특정 모임 정보를 업데이트합니다. 모임 ID를 기반으로 기존 모임 데이터를 조회 및 검증한 후,
 *   클라이언트 요청 데이터로 업데이트 작업을 수행합니다.
 *
 * 예외처리:
 * - 모임 ID에 해당하는 데이터가 존재하지 않는 경우: BusinessException(ErrorCode.MEETING_NOT_FOUND)
 * - 요청한 사용자가 모임 호스트가 아닌 경우: BusinessException(ErrorCode.FORBIDDEN)
 * - 수정하려는 마트 ID가 데이터베이스에 존재하지 않는 경우: BusinessException(ErrorCode.MART_NOT_FOUND)
 *
 * @author 고동현
 * @since - 2025-09-15
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UpdateMeetingService {

    private final MeetingsRepository meetingsRepository;
    private final MartsRepository martsRepository;

    public Meeting updateMeeting(Long meetingId, MeetingUpdateRequest request, Long userId) {
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        if (!meeting.getHostUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Mart newMart = martsRepository.findById(request.getMartId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MART_NOT_FOUND));

        // Meeting 객체에게 직접 업데이트를 위임
        meeting.update(request, newMart);


        log.info("모임 정보가 수정되었습니다. meetingId: {}, userId: {}", meetingId, userId);

        return meeting;
    }
}