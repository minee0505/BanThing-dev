package com.nathing.banthing.service;

import com.nathing.banthing.dto.request.CommentCreateDto;
import com.nathing.banthing.dto.response.CommentReadDto;
import com.nathing.banthing.dto.response.CommentListDto;
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

        if (!isParticipant) {
            throw new IllegalArgumentException("댓글을 열람할 권한이 없습니다. 모임에 참여한 사용자만 댓글을 볼 수 있습니다.");
        }

        // 3. 모임 ID로 댓글 목록을 조회 (최신순 정렬)
        List<Comment> comments = commentRepository.findByMeetingMeetingIdOrderByCreatedAtDesc(meetingId);

        // 4. 엔티티를 DTO로 변환
        List<CommentReadDto> commentDtos = comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 5. CommentListDto로 최종 응답 구성
        CommentListDto commentListDto = new CommentListDto();
        commentListDto.setComments(commentDtos);
        commentListDto.setTotalCount(commentDtos.size());

        return commentListDto;
    }

    @Override
    @Transactional
    public CommentReadDto createComment(Long meetingId, Long currentUserId, CommentCreateDto dto) {
        // 1. 사용자 및 모임 존재 여부 확인
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));

        // 2. 현재 사용자가 해당 모임의 유효한 참여자인지 확인 (비즈니스 로직)
        boolean isParticipant = meetingParticipantRepository.findByMeetingMeetingIdAndUserUserIdAndApplicationStatus(
                meetingId,
                currentUserId,
                MeetingParticipant.ApplicationStatus.APPROVED
        ).isPresent();

        if (!isParticipant) {
            throw new IllegalArgumentException("댓글을 작성할 권한이 없습니다. 모임에 참여하고 승인된 사용자만 댓글을 작성할 수 있습니다.");
        }

        // 3. Comment 엔티티 생성 및 저장
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setMeeting(meeting);
        comment.setContent(dto.getContent());

        Comment savedComment = commentRepository.save(comment);

        // 4. 엔티티를 DTO로 변환하여 반환
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
}