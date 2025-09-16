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

    @Override
    public CommentListDto getCommentsByMeetingId(Long meetingId, String providerId) {
        // 1. 모임 존재 여부 확인
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        // ✅ 댓글 조회는 로그인 여부와 관계없이 모두에게 허용
        // 2. 현재 사용자가 해당 모임의 유효한 참여자인지 확인 (인증 로직 제거)
//        boolean isParticipant = meetingParticipantRepository.findByMeetingMeetingIdAndUserUserIdAndApplicationStatus(
//                meetingId,
//                currentUserId,
//                ApplicationStatus.APPROVED
//        ).isPresent();

        // 3. 댓글 목록 조회
        List<Comment> comments = commentRepository.findByMeetingMeetingIdOrderByCreatedAtDesc(meetingId);

        // 4. DTO로 변환
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
    public CommentReadDto createComment(Long meetingId, String providerId, String content) {
        // 1. providerId로 사용자 및 모임 존재 여부 확인
        // userId가 아닌 providerId로 사용자를 조회합니다.
        User user = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));

        // 2. 승인된 참여자 및 호스트만 댓글 작성 가능하도록 검증
        // user.getUserId()를 사용해 정확한 userId를 전달합니다.
        boolean isApprovedParticipant = meetingParticipantRepository
                .findByMeetingMeetingIdAndUserUserIdAndApplicationStatus(meetingId, user.getUserId(), ApplicationStatus.APPROVED)
                .isPresent();

        // hostUser의 userId와 user의 userId를 비교해야 합니다.
        boolean isHost = meeting.getHostUser().getUserId().equals(user.getUserId());

        /* 테스트용 주석으로 테스터가 모임의 참여 인원이 아니라도 해당 기능을 테스트할 수 있게 주석처리함
        if (!isApprovedParticipant && !isHost) {
            throw new IllegalArgumentException("댓글을 작성할 권한이 없습니다. 모임에 참여하고 승인된 사용자만 댓글을 작성할 수 있습니다.");
        }*/

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
    public CommentReadDto updateComment(Long commentId, String providerId, String content) {
        // 1. 댓글 존재 여부 확인 및 엔티티 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 2. 현재 사용자가 댓글의 작성자인지 확인
        // providerId가 아닌 user.getProviderId()를 사용하여 비교합니다.
        /* 테스트용 주석으로 테스터가 모임의 참여 인원이 아니라도 해당 기능을 테스트할 수 있게 주석처리함
        if (!comment.getUser().getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다. 작성자만 수정할 수 있습니다.");
        }*/

        // 3. 댓글 내용 수정 (더티 체킹 활용)
        comment.setContent(content);

        // 4. DTO로 변환하여 반환
        return convertToDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String providerId) {
        // 1. 댓글 존재 여부 확인 및 엔티티 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 2. 현재 사용자가 댓글의 작성자인지 확인
        /* 테스트용 주석으로 테스터가 모임의 참여 인원이 아니라도 해당 기능을 테스트할 수 있게 주석처리함
        if (!comment.getUser().getUserId().equals(providerId)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다. 작성자만 삭제할 수 있습니다.");
        }*/

        // 3. 댓글 삭제
        commentRepository.delete(comment);
    }
}