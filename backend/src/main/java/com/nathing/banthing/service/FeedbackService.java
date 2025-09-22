package com.nathing.banthing.service;

import com.nathing.banthing.dto.enums.FeedbackSearchType;
import com.nathing.banthing.dto.request.FeedbackCreateRequest;
import com.nathing.banthing.dto.response.FeedbackResponse;
import com.nathing.banthing.entity.*;
import com.nathing.banthing.repository.FeedbacksRepository;
import com.nathing.banthing.repository.MeetingParticipantsRepository; // 가정: 이 리포지토리가 존재합니다.
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final UsersRepository usersRepository;
    private final MeetingsRepository meetingsRepository;
    private final FeedbacksRepository feedbacksRepository;
    private final MeetingParticipantsRepository meetingParticipantsRepository; // 추가

    @Transactional
    public User createFeedback(FeedbackCreateRequest dto, String  giverIdStr, String receiverIdStr) {
        // meetingId 유효성 검사
        Meeting meeting = meetingsRepository.findById(dto.getMeetingId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid meetingID: " + dto.getMeetingId()));
        /*// receiverId 유효성 검사
        User receiverUser = usersRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid receiverID: " + dto.getReceiverId()));
        // giverId 유효성 검사
        User giverUser = usersRepository.findById(giverId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid giverID: " + giverId));*/

        // receiverUser를 userId로 변환하는 로직 추가
        User receiverUser = findUserByIdentifier(receiverIdStr);

        // giverUser를 userId로 변환하는 로직 추가
        User giverUser = findUserByIdentifier(giverIdStr);

        // 1. 피드백을 주는 사용자와 받는 사용자가 동일인물인지 확인 [cite: 1]
        if (giverUser.equals(receiverUser)) {
            throw new IllegalArgumentException("자기 자신에게 피드백을 줄 수 없습니다.");
        }

        // 2. 두 사람 모두 해당 모임에 참여했는지 확인
        boolean giverParticipated = meetingParticipantsRepository.existsByMeetingAndUserAndApplicationStatus(meeting, giverUser, MeetingParticipant.ApplicationStatus.APPROVED);
        boolean receiverParticipated = meetingParticipantsRepository.existsByMeetingAndUserAndApplicationStatus(meeting, receiverUser, MeetingParticipant.ApplicationStatus.APPROVED);

        if (!giverParticipated || !receiverParticipated) {
            throw new IllegalArgumentException("피드백을 주거나 받는 사용자가 해당 모임의 참여자가 아닙니다.");
        }

        // 3. 피드백을 주는 상대에게 이미 피드백을 줬는지 확인
        if (feedbacksRepository.existsByMeetingAndGiverUserAndReceiverUser(meeting, giverUser, receiverUser)) {
            throw new IllegalArgumentException("이미 해당 모임에서 해당 사용자에게 피드백을 주셨습니다.");
        }

        /// 피드백 타입에 따라 점수 이벤트 매핑
        ScoreEvent event;
        if ("POSITIVE".equals(dto.getFeedbackType())) {
            event = ScoreEvent.POSITIVE;
        } else if ("NEGATIVE".equals(dto.getFeedbackType())) {
            event = ScoreEvent.NEGATIVE;
        } else {
            throw new IllegalArgumentException("Invalid feedback type: " + dto.getFeedbackType());
        }

        // Feedback 엔티티에 저장
        // feedbackType 필드를 사용하여 피드백을 생성
        Feedback feedback = Feedback.builder()
                .meeting(meeting)
                .giverUser(giverUser)
                .receiverUser(receiverUser)
                .feedbackType(FeedbackType.valueOf(dto.getFeedbackType())) // 새로운 필드를 사용
                .build();
        feedbacksRepository.save(feedback);

        // 받는 사용자의 신뢰도 점수 업데이트
        receiverUser.updateTrustScore(event);
        usersRepository.save(receiverUser);

        // 업데이트된 User 엔티티를 반환합니다.
        return receiverUser;
    }

    // giverUser(userId 또는 nickname)로 User를 찾는 헬퍼 함수
    private User findUserByIdentifier(String identifier) {
        try {
            // 1. 숫자로 변환을 시도하여 userId인지 확인
            Long userId = Long.parseLong(identifier);
            return usersRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        } catch (NumberFormatException e) {
            // 2. 숫자가 아니라면 nickname으로 간주하고 사용자 조회
            return usersRepository.findByNickname(identifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with nickname: " + identifier));
        }
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByUserAndType(Long userId, FeedbackSearchType searchType) {
        // userId 유효성 검사
        usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        List<Feedback> feedbacks;
        if (searchType == FeedbackSearchType.RECEIVED) {
            feedbacks = feedbacksRepository.findByReceiverUser_UserId(userId);
        } else if (searchType == FeedbackSearchType.GIVEN) {
            feedbacks = feedbacksRepository.findByGiverUser_UserId(userId);
        } else {
            throw new IllegalArgumentException("Invalid feedback search type");
        }

        // Feedback 엔티티 리스트를 FeedbackResponse DTO 리스트로 변환
        return feedbacks.stream()
                .map(FeedbackResponse::from)
                .collect(Collectors.toList());
    }


}