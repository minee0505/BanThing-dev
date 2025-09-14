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

        // --- 수정 가능한 필드만 덮어쓰도록 변경 ---
        meeting.setMart(newMart);
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setMeetingDate(request.getMeetingDate());
        meeting.setThumbnailImageUrl(request.getThumbnailImageUrl());


        log.info("모임 정보가 수정되었습니다. meetingId: {}, userId: {}", meetingId, userId);

        return meeting;
    }
}