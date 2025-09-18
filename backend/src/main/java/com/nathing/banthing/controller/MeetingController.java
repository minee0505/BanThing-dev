package com.nathing.banthing.controller;

import com.nathing.banthing.dto.common.ApiResponse;
import com.nathing.banthing.dto.request.MeetingCreateRequest;
import com.nathing.banthing.dto.request.MeetingUpdateRequest;
import com.nathing.banthing.dto.response.*;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * MeetingController는 모임과 관련된 다양한 기능을 제공하는 REST API 컨트롤러 클래스입니다.
 *
 * 이 컨트롤러는 모임의 생성, 조회, 수정, 삭제, 참가 신청, 모집 마감 등
 * 모임 관리와 관련된 모든 주요 작업에 대해 HTTP 요청을 처리합니다.
 * 각 메서드는 특정 작업을 수행하며, 요청 경로, HTTP 메서드, 요청 데이터 및
 * 응답 데이터를 명확히 정의합니다.
 *
 * 주요 기능:
 * - 모임 생성
 * - 전체 모임 목록 조회
 * - 특정 모임 상세 조회
 * - 모임 수정
 * - 모임 삭제
 * - 참가 신청 및 신청 목록 조회
 * - 참가 신청 승인
 * - 모집 마감 기능
 * - 특정 사용자 참여 모임 목록 조회
 *
 * 각 메서드는 클라이언트가 API 호출을 통해 모임 데이터를 관리할 수 있도록 하며,
 * 데이터의 유효성 검증 및 인증, 권한 체크 등의 작업을 포함합니다.
 *
 * 의존 서비스:
 * - CreateMeetingService: 모임 생성 관련 로직 처리
 * - FindMeetingService: 모임 조회 관련 로직 처리
 * - UpdateMeetingService: 모임 수정 관련 로직 처리
 * - DeleteMeetingService: 모임 삭제 관련 로직 처리
 * - JoinMeetingService: 모임 참가 신청 및 승인 관련 로직 처리
 * - ManageMeetingService: 모임 관리(참가 신청 승인, 모집 마감 등) 관련 로직 처리
 *
 * 유효성 검사와 인증:
 * - `@Valid` 어노테이션을 활용하여 요청 데이터의 유효성을 검사합니다.
 * - `@AuthenticationPrincipal` 어노테이션으로 현재 사용자의 인증 정보를 자동 주입합니다.
 *
 * @author 고동현
 * @since - 2025-09-15
 */

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Slf4j
public class MeetingController {

    private final CreateMeetingService createMeetingService;
    private final FindMeetingService findMeetingService;
    private final MeetingsRepository meetingsRepository;
    private final UpdateMeetingService updateMeetingService;
    private final DeleteMeetingService deleteMeetingService;
    private final JoinMeetingService joinMeetingService;
    private final ManageMeetingService manageMeetingService;



    /**
     * 모임 생성 API (파일 업로드 기능 추가)
     * @param request 모임 생성 요청 데이터 (JSON)
     * @param imageFile 업로드된 이미지 파일 (선택)
     * @param providerId 사용자 ID
     * @return 생성된 모임 ID를 포함한 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MeetingCreateResponse>> createMeeting(
            @RequestPart("request") @Valid MeetingCreateRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal String providerId) {

        Meeting newMeeting = createMeetingService.createMeeting(request, imageFile, providerId);
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
    public ResponseEntity<ApiResponse<List<MeetingSimpleResponse>>> getAllMeetings(
            @RequestParam(required = false) String keyword) { //  수정: keyword를 쿼리 파라미터로 받습니다.

        //  수정: 서비스 계층의 새로운 검색 메서드를 호출합니다.
        List<MeetingSimpleResponse> meetings = findMeetingService.searchMeetings(keyword);

        ApiResponse<List<MeetingSimpleResponse>> apiResponse = ApiResponse.success("모임 목록이 성공적으로 조회되었습니다.", meetings);

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
            @AuthenticationPrincipal String providerId,
            @Valid @RequestBody MeetingUpdateRequest request) {

        Meeting updatedMeeting = updateMeetingService.updateMeeting(meetingId, request, providerId);
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
            @AuthenticationPrincipal String providerId) {


        deleteMeetingService.deleteMeeting(meetingId, providerId);

        // 삭제 후에는 별도 데이터 없이 성공 메시지만 반환합니다.
        ApiResponse<Void> apiResponse = ApiResponse.success("모임이 성공적으로 삭제되었습니다.", null);

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 특정 사용자 참여 모임 목록 조회 API
     *
     * @param page 페이지 번호를 나타냅니다. 0부터 시작합니다.
     * @param size 페이지 당 표시할 모임 수를 나타냅니다.
     * @param providerId 현재 인증된 사용자의 제공자 ID를 나타냅니다.
     * @return 사용자가 참여한 모임 목록을 포함하는 ResponseEntity 객체를 반환합니다.
     *
     * @author 강관주
     * @since 2025-09-18
     */
    @GetMapping("/participated")
    public ResponseEntity<?> getParticipatedMeetings(
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal String providerId) {
        log.info("참여한 모임 목록 조회 API 호출 - 페이지: {}, 크기: {}", page, size);

        // 페이지 변환
        Pageable pageable = PageRequest.of(page, size);

        // 서비스 계층 호출
        MeetingParticipatedPageResponse dto = findMeetingService.getParticipatedMeetings(providerId, pageable);

        // 공통 응답 포맷으로 감싸기
        ApiResponse<MeetingParticipatedPageResponse> response = ApiResponse.success("모임 목록이 성공적으로 조회되었습니다.", dto);

        return ResponseEntity.ok(response);

    }


    // ================================ 여기서 부터는 모임 신청 관련 api ================================== /



    /**
     * 모임 참가 신청 API
     *
     * @param meetingId 참가 신청할 모임의 ID
     * @param providerId 현재 로그인한 사용자의 ID (자동 주입)
     * @return 성공 응답
     */
    @PostMapping("/{meetingId}/join")
    public ResponseEntity<ApiResponse<Void>> joinMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal String providerId) {

        joinMeetingService.joinMeeting(meetingId, providerId);

        ApiResponse<Void> apiResponse = ApiResponse.success("모임 참가 신청이 완료되었습니다.", null);

        return ResponseEntity.ok(apiResponse);
    }


    /**
     * 모임 참가자 목록 조회 API (호스트 전용)
     * 호스트에게는 확정된 멤버와 신청 대기자 목록을 모두 반환합니다.
     *
     * @param meetingId 신청 목록을 조회할 모임의 ID
     * @param providerId 현재 로그인한 사용자의 ID (자동 주입)
     * @return 참가자 목록 (확정/대기 포함)
     */
    @GetMapping("/{meetingId}/participants")
    public ResponseEntity<ApiResponse<ParticipantListResponse>> getParticipants(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal String providerId) {

        ParticipantListResponse participants = joinMeetingService.getParticipantsByStatusForHost(meetingId, providerId);

        ApiResponse<ParticipantListResponse> apiResponse = ApiResponse.success("참가자 목록이 성공적으로 조회되었습니다.", participants);

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 참가자의 모임 참가 신청을 승인하는 메서드입니다.
     *
     * 이 API는 주최자(호스트)가 특정 모임(meetingId)에 대해 특정 참가자(participantId)의
     * 참가 신청을 승인할 때 호출됩니다. 성공적으로 승인되었을 경우,
     * 응답으로 성공 메시지와 상태 코드 200을 반환합니다.
     *
     * @param meetingId 모임의 고유 식별자(ID)입니다.
     * @param participantId 승인할 참가자의 고유 식별자(ID)입니다.
     * @param hostProviderId 현재 인증된 사용자의 고유 식별자(ID)로서, 주최자(호스트)를 나타냅니다.
     * @return 참가 신청 승인 작업이 성공적으로 완료되었음을 나타내는 응답 객체입니다.
     */
    @PostMapping("/{meetingId}/participants/{participantId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveParticipant(
            @PathVariable Long meetingId,
            @PathVariable Long participantId,
            @AuthenticationPrincipal String hostProviderId) {
        manageMeetingService.approveParticipant(meetingId, participantId, hostProviderId);
        return ResponseEntity.ok(ApiResponse.success("참가 신청이 승인되었습니다.", null));
    }

    /**
     * 모집을 마감하는 API 핸들러 메서드입니다.
     *
     * 이 메서드는 주어진 모임 ID와 호스트 ID를 사용하여 모집 마감 로직을 수행합니다.
     * 성공적으로 실행된 경우, 모집이 마감되었다는 메시지를 포함한 응답을 반환합니다.
     *
     * @param meetingId 모집을 마감할 모임의 고유 식별자 (PathVariable로 전달됨)
     * @param hostProviderId 현재 요청을 보낸 사용자의 ID로, 인증 정보에서 추출됨 (AuthenticationPrincipal로 전달됨)
     * @return 모집 마감 성공 메시지가 포함된 응답
     */
    @PostMapping("/{meetingId}/close-recruitment")
    public ResponseEntity<ApiResponse<Void>> closeRecruitment(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal String hostProviderId) {
        manageMeetingService.closeRecruitment(meetingId, hostProviderId);
        return ResponseEntity.ok(ApiResponse.success("모집이 마감되었습니다.", null));
    }

    /**
     * 사용자가 특정 모임에서 탈퇴하는 기능을 처리하는 메서드입니다.
     *
     * 요청 경로의 모임 ID와 인증된 사용자 ID를 이용하여 사용자가 해당 모임에서 탈퇴하도록 처리합니다.
     * 성공적으로 처리된 경우, 성공 메시지와 함께 응답이 반환됩니다.
     *
     * @param meetingId 탈퇴하려는 모임의 고유 식별자 (path variable)
     * @param providerId 인증된 사용자의 고유 식별자 (authentication principal)
     * @return ApiResponse 객체를 감싼 ResponseEntity로 처리 결과를 반환합니다. 성공적으로 처리된 경우 "모임에서 탈퇴하였습니다." 메시지가 포함됩니다.
     */
    @PostMapping("/{meetingId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal String providerId) {
        manageMeetingService.leaveMeeting(meetingId, providerId);
        return ResponseEntity.ok(ApiResponse.success("모임에서 탈퇴하였습니다.", null));
    }

    /**
     * 주어진 모임 ID와 호스트 ID를 기반으로 모임을 완료 상태로 변경하는 메서드입니다.
     * 모임 완료 처리가 성공적으로 이루어진 경우, 적절한 API 응답 메시지를 반환합니다.
     *
     * @param meetingId 완료하고자 하는 모임의 식별자(ID)
     * @param hostProviderId 완료 처리를 요청하는 호스트 사용자의 식별자(ID)
     * @return 모임 완료 처리 결과를 포함하는 ResponseEntity 객체
     */
    @PostMapping("/{meetingId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal String hostProviderId) {
        manageMeetingService.completeMeeting(meetingId, hostProviderId);
        return ResponseEntity.ok(ApiResponse.success("모임이 종료되었습니다.", null));
    }



}