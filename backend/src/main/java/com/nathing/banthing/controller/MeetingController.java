package com.nathing.banthing.controller;

import com.nathing.banthing.dto.common.ApiResponse;
import com.nathing.banthing.dto.request.MeetingCreateRequest;
import com.nathing.banthing.dto.request.MeetingUpdateRequest;
import com.nathing.banthing.dto.response.MeetingCreateResponse;
import com.nathing.banthing.dto.response.MeetingDetailResponse;
import com.nathing.banthing.dto.response.MeetingSimpleResponse;
import com.nathing.banthing.dto.response.MeetingUpdateResponse;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.service.CreateMeetingService;
import com.nathing.banthing.service.DeleteMeetingService;
import com.nathing.banthing.service.FindMeetingService;
import com.nathing.banthing.service.UpdateMeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<MeetingCreateResponse>> createMeeting(@Valid @RequestBody MeetingCreateRequest request) {
        // TODO: 인증 로직 추가 후 userId를 실제 토큰에서 가져오도록 수정해야 합니다.
        Long currentUserId = 1L; // 임시로 사용자 ID를 1로 설정

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
            @Valid @RequestBody MeetingUpdateRequest request) {

        // TODO: 인증 로직 추가 후 userId를 실제 토큰에서 가져오도록 수정해야 합니다.
        Long currentUserId = 1L;


        Meeting updatedMeeting = updateMeetingService.updateMeeting(meetingId, request, currentUserId);

        // 6. 전용 응답 DTO로 변환하여 반환
        MeetingUpdateResponse responseDto = new MeetingUpdateResponse(updatedMeeting);
        ApiResponse<MeetingUpdateResponse> apiResponse = ApiResponse.success("모임 정보가 성공적으로 수정되었습니다.", responseDto);

        return ResponseEntity.ok(apiResponse);
    }


    /**
     * 모임 삭제 API
     * @param meetingId 삭제할 모임의 ID
     * @return 성공 응답
     */
    @DeleteMapping("/delete/{meetingId}")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(@PathVariable Long meetingId) {
        // TODO: 인증 로직이 완성되면 실제 토큰에서 userId를 가져와야 합니다.
        Long currentUserId = 1L; // 임시 사용자 ID (호스트라고 가정)

        deleteMeetingService.deleteMeeting(meetingId, currentUserId);

        // 삭제 후에는 별도 데이터 없이 성공 메시지만 반환합니다.
        ApiResponse<Void> apiResponse = ApiResponse.success("모임이 성공적으로 삭제되었습니다.", null);

        return ResponseEntity.ok(apiResponse);
    }
}