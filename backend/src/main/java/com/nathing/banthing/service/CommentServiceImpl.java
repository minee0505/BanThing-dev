// CommentServiceImpl.java
package com.nathing.banthing.service;

import com.nathing.banthing.dto.response.CommentListDto;
import com.nathing.banthing.dto.response.CommentReadDto;
import com.nathing.banthing.entity.Comment;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.MeetingParticipant;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.entity.MeetingParticipant.ApplicationStatus;
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
    private final UsersRepository userRepository;

    @Override
    public CommentListDto getCommentsByMeetingId(Long meetingId, Long currentUserId) {
        // 1. 모임 존재 여부 확인
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));

        // 2. 현재 사용자가 해당 모임의 유효한 참여자인지 확인 (비즈니스 로직)
        boolean isParticipant = meetingParticipantRepository.findByMeetingMeetingIdAndUserUserIdAndApplicationStatus(
                meetingId,
                currentUserId,
                ApplicationStatus.APPROVED
        ).isPresent();

        // 3. 모임 호스트도 댓글을 열람할 수 있도록 예외 처리 추가
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        boolean isHost = meeting.getHostUser().getUserId().equals(currentUserId);

        if (!isParticipant && !isHost) {
            throw new IllegalArgumentException("댓글을 열람할 권한이 없습니다. 모임에 참여한 사용자만 댓글을 볼 수 있습니다.");
        }

        // 4. 모임 ID로 댓글 목록을 조회 (최신순 정렬)
        List<Comment> comments = commentRepository.findByMeetingMeetingIdOrderByCreatedAtDesc(meetingId);

        // 5. 엔티티를 DTO로 변환
        List<CommentReadDto> commentDtos = comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 6. CommentListDto로 최종 응답 구성
        CommentListDto commentListDto = new CommentListDto();
        commentListDto.setComments(commentDtos);
        commentListDto.setTotalCount(commentDtos.size());

        return commentListDto;
    }

    @Override
    @Transactional
    public CommentReadDto createComment(Long meetingId, Long currentUserId, String content) {
        // 1. 사용자 및 모임 존재 여부 확인
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));

        // 2. 승인된 참여자 및 호스트만 댓글 작성 가능하도록 검증
        boolean isApprovedParticipant = meetingParticipantRepository
                .findByMeetingMeetingIdAndUserUserIdAndApplicationStatus(meetingId, currentUserId, ApplicationStatus.APPROVED)
                .isPresent();
        boolean isHost = meeting.getHostUser().getUserId().equals(currentUserId);


        if (!isApprovedParticipant && !isHost) {
            throw new IllegalArgumentException("댓글을 작성할 권한이 없습니다. 모임에 참여하고 승인된 사용자만 댓글을 작성할 수 있습니다.");
        }

        // 3. 댓글 엔티티 생성
        Comment comment = Comment.builder()
                .meeting(meeting)
                .user(user)
                .content(content)
                .build();

        // 4. 댓글 저장
        Comment savedComment = commentRepository.save(comment);

        // 5. DTO로 변환하여 반환
        return convertToDto(savedComment);
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

    @Override
    @Transactional
    public CommentReadDto updateComment(Long commentId, Long currentUserId, String content) {
        // 1. 댓글 존재 여부 확인 및 엔티티 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 2. 현재 사용자가 댓글의 작성자인지 확인
        if (!comment.getUser().getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다. 작성자만 수정할 수 있습니다.");
        }

        // 3. 댓글 내용 수정 (더티 체킹 활용)
        comment.setContent(content);

        // 4. DTO로 변환하여 반환
        return convertToDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        // 1. 댓글 존재 여부 확인 및 엔티티 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 2. 현재 사용자가 댓글의 작성자인지 확인
        if (!comment.getUser().getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다. 작성자만 삭제할 수 있습니다.");
        }

        // 3. 댓글 삭제
        commentRepository.delete(comment);
    }
}