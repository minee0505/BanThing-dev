package com.nathing.banthing.service;

import com.nathing.banthing.dto.request.CommentCreateDto;
import com.nathing.banthing.dto.response.CommentListDto;
import com.nathing.banthing.dto.response.CommentReadDto;

public interface CommentService {
    /**
     * 특정 모임에 속한 댓글 목록을 조회합니다.
     * @param meetingId 모임 ID
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 댓글 목록 DTO (CommentListDto)
     */
    CommentListDto getCommentsByMeetingId(Long meetingId, Long currentUserId);

    /**
     * 특정 모임에 새로운 댓글을 작성합니다.
     * @param meetingId 모임 ID
     * @param createDto 댓글 생성 요청 DTO
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 생성된 댓글 DTO (CommentReadDto)
     */
    CommentReadDto createComment(Long meetingId,  Long currentUserId, CommentCreateDto createDto);
}