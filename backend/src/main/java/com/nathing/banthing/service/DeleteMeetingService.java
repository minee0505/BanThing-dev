package com.nathing.banthing.service;

import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.MeetingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DeleteMeetingService {

    private final MeetingsRepository meetingsRepository;

    /**
     * 모임 논리적 삭제
     * @param meetingId 삭제할 모임의 ID
     * @param providerId 요청을 보낸 사용자의 ID (권한 확인용)
     */
    public void deleteMeeting(Long meetingId, String providerId) {
        // 1. 삭제할 모임을 조회합니다.
        //    (@Where 어노테이션 덕분에 이미 삭제된 모임은 여기서 조회되지 않습니다.)
        Meeting meeting = meetingsRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        // 2. 요청을 보낸 사용자가 모임의 호스트인지 권한을 확인합니다.
        if (!meeting.getHostUser().getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 3. 'delete'를 호출하면, JPA가 @SQLDelete에 정의된 쿼리를 실행하여
        //    deleted_at 컬럼을 업데이트합니다. (논리적 삭제)
        meetingsRepository.delete(meeting);

        log.info("모임이 논리적으로 삭제되었습니다. meetingId: {}, userId: {}", meetingId, providerId);
    }
}