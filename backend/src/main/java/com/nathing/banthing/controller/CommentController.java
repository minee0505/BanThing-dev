package com.nathing.banthing.controller;

import com.nathing.banthing.dto.request.CommentCreateDto;
import com.nathing.banthing.dto.response.CommentListDto;
import com.nathing.banthing.dto.response.CommentReadDto;
import com.nathing.banthing.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/meetings/{meetingId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 특정 모임의 댓글 목록을 조회합니다.
     * @param meetingId 모임 ID
     * @param userDetails 현재 로그인한 사용자 정보 (스프링 시큐리티)
     * @return 댓글 목록 DTO와 HTTP 상태 코드
     */
    @GetMapping
    public ResponseEntity<CommentListDto> getCommentsByMeetingId(
            @PathVariable Long meetingId
//            @AuthenticationPrincipal UserDetails userDetails
        ) {
        // ⚠️ 경고: 이 방법은 임시 개발용입니다. 실제 배포 시에는 인증 로직을 구현해야 합니다.
        Long currentUserId = 2L; // 2번 사용자로 임시 지정
        // 실제 애플리케이션에서는 UserDetails에서 사용자 ID를 추출하는 로직 필요
//        Long currentUserId = Long.valueOf(userDetails.getUsername());
        CommentListDto commentList = commentService.getCommentsByMeetingId(meetingId, currentUserId);
        return ResponseEntity.ok(commentList);
    }

    /**
     * 특정 모임에 새로운 댓글을 작성합니다.
     * @param meetingId 모임 ID
     * @param createDto 댓글 생성 요청 DTO
     * @param userDetails 현재 로그인한 사용자 정보 (스프링 시큐리티)
     * @return 생성된 댓글 DTO와 HTTP 상태 코드
     */
    @PostMapping
    public ResponseEntity<CommentReadDto> createComment(
            @PathVariable Long meetingId,
            @RequestBody CommentCreateDto createDto
//            @AuthenticationPrincipal UserDetails userDetails
        ) {
        // ⚠️ 경고: 이 방법은 임시 개발용입니다. 실제 배포 시에는 인증 로직을 구현해야 합니다.
        Long currentUserId = 2L; // 2번 사용자로 임시 지정
        // 요청 DTO의 meetingId 필드를 URL 경로의 meetingId로 덮어씌움
        createDto.setMeetingId(meetingId);
        // 실제 애플리케이션에서는 UserDetails에서 사용자 ID를 추출하는 로직 필요
//        Long currentUserId = Long.valueOf(userDetails.getUsername());

        CommentReadDto newComment = commentService.createComment(meetingId, currentUserId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }
}