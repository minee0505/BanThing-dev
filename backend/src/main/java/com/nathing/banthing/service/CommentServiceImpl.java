// CommentServiceImpl.java
package com.nathing.banthing.service;

import com.nathing.banthing.dto.response.CommentListDto;
import com.nathing.banthing.dto.response.CommentReadDto;
import com.nathing.banthing.entity.Comment;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.entity.MeetingParticipant.ApplicationStatus;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.CommentRepository;
import com.nathing.banthing.repository.MeetingParticipantsRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final MeetingsRepository meetingRepository;
    private final MeetingParticipantsRepository meetingParticipantRepository;
    private final UsersRepository usersRepository;


    /**
     해당 모임ID의 댓글 조회(논리적 삭제되지 않은 댓글만)
     * @param meetingId 모임 ID
     * @param providerId 현재 로그인한 사용자 ID
     * @return
     */
    @Override
    public CommentListDto getCommentsByMeetingId(Long meetingId, String providerId) {
        // 1. 모임 존재 여부 확인
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        //  2. 댓글 조회는 로그인 여부와 관계없이 모두에게 허용

        // 3. 댓글 목록 조회
        List<Comment> comments = commentRepository.findByMeetingMeetingIdOrderByCreatedAtDesc(meetingId);

        // 4. DTO로 변환
        List<CommentReadDto> commentDtos = comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 5. CommentListDto로 최종 응답 구성
        CommentListDto commentListDto = new CommentListDto();
        commentListDto.setComments(commentDtos);
        commentListDto.setTotalCount(commentDtos.size());

        return commentListDto;
    }


    /**
     * 특정 모임에 새로운 댓글을 작성하는 서비스
     * @param meetingId 모임 ID
     * @param providerId 로그인한 사용자의 providerId
     * @param content 댓글 내용
     * @return 생성된 댓글 DTO
     */
    @Override
    @Transactional
    public CommentReadDto createComment(Long meetingId, String providerId, String content) {
        // 1. 사용자 및 모임 존재 확인
        User user = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        // 2. 로그인 여부 및 모임 참여 여부 확인
        boolean isParticipant = meetingParticipantRepository.existsByMeetingAndUserAndApplicationStatus(
                meeting, user, ApplicationStatus.APPROVED);

        if (!isParticipant) {
            throw new IllegalArgumentException("댓글을 작성할 권한이 없습니다. 모임에 참여하고 승인된 사용자만 댓글을 작성할 수 있습니다.");
        }

        // 3. 댓글 엔티티 생성 및 저장
        Comment newComment = Comment.builder()
                .content(content)
                .meeting(meeting)
                .user(user)
                .build();
        commentRepository.save(newComment);

        return convertToDto(newComment);
    }

    /**
     * Comment 엔티티를 CommentReadDto로 변환하는 헬퍼 메서드
     */
    private CommentReadDto convertToDto(Comment comment) {
        CommentReadDto dto = new CommentReadDto();
        dto.setCommentId(comment.getCommentId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        User user = comment.getUser();
        dto.setUserId(user.getUserId());
        dto.setNickname(user.getNickname());
        dto.setProfileImageUrl(user.getProfileImageUrl());

        return dto;
    }


    /**
     댓글 수정 서비스
     * @param commentId 수정할 댓글 ID
     * @param providerId
     * @param content 새로운 댓글 내용
     * @return
     */
    @Override
    @Transactional
    public CommentReadDto updateComment(Long commentId, String providerId, String content) {
        // 1. 로그인한 사용자 존재 여부 확인
        usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 댓글 존재 여부 확인 및 엔티티 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 3. 모임 호스트 및 댓글 작성자 권한 확인
        // 현재 사용자가 댓글 작성자이거나 모임 호스트인지 확인하는 로직 추가
        Meeting meeting = comment.getMeeting();
        boolean isHost = meeting.getHostUser().getProviderId().equals(providerId);
        boolean isAuthor = comment.getUser().getProviderId().equals(providerId);

        if (!isHost && !isAuthor) {
            throw new BusinessException(ErrorCode.FORBIDDEN_COMMENT);
        }

        // 4. 현재 사용자가 댓글의 작성자인지 확인
        if (!comment.getUser().getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_COMMENT);
        }

        // 5. 댓글 내용 수정
        comment.setContent(content);

        return convertToDto(comment);
    }


    /**
     댓글 논리적 삭제 서비스
     * @param commentId 삭제할 댓글 ID
     * @param providerId
     */
    @Override
    @Transactional
    public void deleteComment(Long commentId, String providerId) {
        // 1. 로그인한 사용자 존재 여부 확인
        usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 댓글 존재 여부 확인 및 엔티티 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 3. 모임 호스트 및 댓글 작성자 권한 확인
        // 현재 사용자가 댓글 작성자이거나 모임 호스트인지 확인하는 로직 추가
        Meeting meeting = comment.getMeeting();
        boolean isHost = meeting.getHostUser().getProviderId().equals(providerId);
        boolean isAuthor = comment.getUser().getProviderId().equals(providerId);

        if (!isHost && !isAuthor) {
            throw new BusinessException(ErrorCode.FORBIDDEN_COMMENT);
        }

        // 4. 현재 사용자가 댓글의 작성자인지 확인
        if (!comment.getUser().getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_COMMENT);
        }

        // 5. 댓글 논리적 삭제
        commentRepository.deleteById(commentId);
    }
}