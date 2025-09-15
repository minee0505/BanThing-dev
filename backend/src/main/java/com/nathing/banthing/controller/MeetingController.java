package com.nathing.banthing.controller;

import com.nathing.banthing.dto.common.ApiResponse;
import com.nathing.banthing.dto.request.MeetingCreateRequest;
import com.nathing.banthing.dto.request.MeetingUpdateRequest;
import com.nathing.banthing.dto.response.MeetingCreateResponse;
import com.nathing.banthing.dto.response.MeetingDetailResponse;
import com.nathing.banthing.dto.response.MeetingSimpleResponse;
import com.nathing.banthing.dto.response.MeetingUpdateResponse;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.service.CreateMeetingService;
import com.nathing.banthing.service.DeleteMeetingService;
import com.nathing.banthing.service.FindMeetingService;
import com.nathing.banthing.service.UpdateMeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 모임 관리와 관련된 REST API를 제공하는 컨트롤러 클래스.
 * API 엔드포인트를 통해 모임 생성, 조회, 수정, 삭제 작업을 수행.
 * @author 고동현
 * @since - 2025-09-15
 */
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final CreateMeetingService createMeetingService;
    private final FindMeetingService findMeetingService;
    private final MeetingsRepository meetingsRepository;
    private final UpdateMeetingService updateMeetingService;
    private final DeleteMeetingService deleteMeetingService;

    /**
     * 모임 생성 API
     *
     * @param request 모임 생성 요청 데이터
     * @return 생성된 모임 ID를 포함한 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MeetingCreateResponse>> createMeeting(
            @Valid @RequestBody MeetingCreateRequest request,
            @AuthenticationPrincipal Long currentUserId) {

        Meeting newMeeting = createMeetingService.createMeeting(request, currentUserId);

        MeetingCreateResponse responseDto = new MeetingCreateResponse(newMeeting);

        ApiResponse<MeetingCreateResponse> apiResponse = ApiResponse.success("모임이 성공적으로 생성되었습니다.", responseDto);

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    /**
     * 전체 모임 목록 조회 API
     *
     * @return 전체 모임 목록
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MeetingSimpleResponse>>> getAllMeetings() {
        List<MeetingSimpleResponse> meetings = findMeetingService.findAllMeetings();

        ApiResponse<List<MeetingSimpleResponse>> apiResponse = ApiResponse.success("전체 모임 목록이 성공적으로 조회되었습니다.", meetings);

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 모임 상세 조회 API
     *
     * @param meetingId 조회할 모임의 ID
     * @return 모임 상세 정보
     */
    @GetMapping("/search/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingDetailResponse>> getMeetingById(@PathVariable Long meetingId) {

        MeetingDetailResponse meetingDetail = findMeetingService.findMeetingById(meetingId);

        ApiResponse<MeetingDetailResponse> apiResponse = ApiResponse.success("모임 상세 정보가 성공적으로 조회되었습니다.", meetingDetail);

        return ResponseEntity.ok(apiResponse);
    }


    /**
     * 모임 수정 API
     *
     * @param meetingId 수정할 모임의 ID
     * @param request   수정할 모임 데이터
     * @return 수정된 모임 ID를 포함한 응답
     */
    @PutMapping("/update/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingUpdateResponse>> updateMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal Long currentUserId,
            @Valid @RequestBody MeetingUpdateRequest request) {


        Meeting updatedMeeting = updateMeetingService.updateMeeting(meetingId, request, currentUserId);

        // 6. 전용 응답 DTO로 변환하여 반환
        MeetingUpdateResponse responseDto = new MeetingUpdateResponse(updatedMeeting);
        ApiResponse<MeetingUpdateResponse> apiResponse = ApiResponse.success("모임 정보가 성공적으로 수정되었습니다.", responseDto);

        return ResponseEntity.ok(apiResponse);
    }


    /**
     * 모임 삭제 API
     *
     * @param meetingId 삭제할 모임의 ID
     * @return 성공 응답
     */
    @DeleteMapping("/delete/{meetingId}")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal Long currentUserId) {


        deleteMeetingService.deleteMeeting(meetingId, currentUserId);

        // 삭제 후에는 별도 데이터 없이 성공 메시지만 반환합니다.
        ApiResponse<Void> apiResponse = ApiResponse.success("모임이 성공적으로 삭제되었습니다.", null);

        return ResponseEntity.ok(apiResponse);
    }
}