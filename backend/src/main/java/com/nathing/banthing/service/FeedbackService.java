package com.nathing.banthing.service;

import com.nathing.banthing.dto.request.FeedbackCreateRequest;
import com.nathing.banthing.dto.response.FeedbackResponse;
import com.nathing.banthing.entity.*;
import com.nathing.banthing.repository.FeedbacksRepository;
import com.nathing.banthing.repository.MeetingParticipantsRepository; // 가정: 이 리포지토리가 존재합니다.
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public void createFeedback(FeedbackCreateRequest dto, Long giverId) {
        // meetingId 유효성 검사
        Meeting meeting = meetingsRepository.findById(dto.getMeetingId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid meetingID: " + dto.getMeetingId()));
        // receiverId 유효성 검사
        User receiverUser = usersRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid receiverID: " + dto.getReceiverId()));
        // giverId 유효성 검사
        User giverUser = usersRepository.findById(giverId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid giverID: " + giverId));


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
    }


    /**
     특정 사용자가 받은 피드백 리스트를 조회합니다.

     @param userId 피드백을 조회할 사용자 ID
     @return FeedbackResponse 리스트
     */
    public List<FeedbackResponse> getFeedbacksByReceiverId(Long userId) {
        // userId 유효성 검사 (사용자 존재 여부 확인)
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        // 해당 사용자가 받은 모든 피드백을 조회
        List<Feedback> feedbacks = feedbacksRepository.findByReceiverUser_UserId(userId);

        // Feedback 엔티티 리스트를 FeedbackResponse DTO 리스트로 변환
        return feedbacks.stream()
                .map(FeedbackResponse::from)
                .collect(Collectors.toList());
    }

    public List<FeedbackResponse> getFeedbacksByGiverId(Long userId) {
        // userId 유효성 검사 (사용자 존재 여부 확인)
        boolean userExists = usersRepository.existsById(userId);
        if (!userExists) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        // 해당 사용자가 준 모든 피드백을 조회
        List<Feedback> feedbacks = feedbacksRepository.findByReceiverUser_UserId(userId);

        // Feedback 엔티티 리스트를 FeedbackResponse DTO 리스트로 변환
        return feedbacks.stream()
                .map(FeedbackResponse::from)
                .collect(Collectors.toList());
    }
}