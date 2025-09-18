package com.nathing.banthing.controller;

import com.nathing.banthing.dto.request.CommentCreateDto;
import com.nathing.banthing.dto.request.CommentUpdateDto;
import com.nathing.banthing.dto.response.CommentListDto;
import com.nathing.banthing.dto.response.CommentReadDto;
import com.nathing.banthing.service.CommentService;
import com.nathing.banthing.service.UserService;
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
    private final UserService userService; // UserService 주입

    /**
     * 특정 모임에 새로운 댓글을 작성합니다.
     * @param meetingId 모임 ID
     * @return 생성된 댓글 DTO와 HTTP 상태 코드
     */
    @PostMapping
    public ResponseEntity<CommentReadDto> createComment(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal String providerId,
            @RequestBody CommentCreateDto createDto) {
        CommentReadDto createdComment = commentService.createComment(meetingId, providerId, createDto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * 특정 모임의 댓글 목록을 조회합니다.
     * @param meetingId 모임 ID
     * @return 댓글 목록 DTO와 HTTP 상태 코드
     */
    @GetMapping
    public ResponseEntity<CommentListDto> getCommentsByMeetingId(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal String providerId) {
        CommentListDto comments = commentService.getCommentsByMeetingId(meetingId, providerId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 특정 댓글을 수정합니다.
     * @param commentId 수정할 댓글 ID
     * @param updateDto 댓글 수정 요청 DTO
     * @return 수정된 댓글 DTO와 HTTP 상태 코드
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentReadDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateDto updateDto,
            @AuthenticationPrincipal String providerId) {

        CommentReadDto updatedComment = commentService.updateComment(commentId, providerId, updateDto.getContent());
        return ResponseEntity.ok(updatedComment);
    }

    /**
     * 특정 댓글을 논리적으로 삭제합니다.
     * @param commentId 삭제할 댓글 ID
     * @return HTTP 상태 코드 (204 No Content)
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String providerId) {

        commentService.deleteComment(commentId, providerId);
        return ResponseEntity.noContent().build();
    }
}