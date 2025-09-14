package com.nathing.banthing.controller;

import com.nathing.banthing.dto.common.ApiResponse;
import com.nathing.banthing.dto.request.MeetingCreateRequest;
import com.nathing.banthing.dto.response.MeetingCreateResponse;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.service.CreateMeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class CreateMeetingController {
    /**
     * CreateMeetingService는 모임 생성 비즈니스 로직을 담당하는 서비스 클래스입니다.
     * 이 변수는 CreateMeetingController에서 사용되며, 모임 생성 요청을 처리하는 데 사용됩니다.
     *
     * 주요 역할:
     * 1. 모임 생성 요청 데이터를 검증하고, 관련 비즈니스 로직을 실행합니다.
     * 2. 사용자, 마트 등의 관련 정보를 조회하고, 이를 기반으로 새로운 모임 엔티티를 생성합니다.
     * 3. 생성된 모임 데이터를 데이터베이스에 저장하고, 필요한 경우 추가 작업(예: 호스트를 모임 참가자로 추가)을 수행합니다.
     *
     * @TODO - 인증 로직 추가 후 userId를 실제 토큰에서 가져오도록 수정해야 합니다.
     * @author - 고동현
     * @Since - 2025-09-15
     */

    private final CreateMeetingService createMeetingService;

    /**
     * 모임 생성 API
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

}
