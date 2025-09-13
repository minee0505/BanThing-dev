package com.nathing.banthing.service;


import com.nathing.banthing.dto.request.FeedbackCreateRequest;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.repository.FeedbacksRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final UsersRepository usersRepository;
    private final MeetingsRepository meetingsRepository;
    private final FeedbacksRepository feedbacksRepository;

    @Transactional
    public void createFeedback(FeedbackCreateRequest dto, Long giverId) {
        // meetingId 츄쵸성 검사
        Meeting meeting = meetingsRepository.findById(dto.getMeetingId()).orElseThrow(() -> new IllegalArgumentException("Invalid meetingID: " + dto.getMeetingId()));
        // receiverId 유효성 검사
        User receiverUser = usersRepository.findById(dto.getReceiverId()).orElseThrow(() -> new IllegalArgumentException("Invalid receiverID: " + dto.getReceiverId()));
        // giverId 유휴성 검사
        User giverUser = usersRepository.findById(dto.getGiverId()).orElseThrow(() -> new IllegalArgumentException("Invalid senderID: " + dto.getGiverId()));
        /*처리로직 구현
        1. meetingRepository를 사용해 모임상태(COMPLETED)를 확인
        2. meetingParticipantsRepository를 사용해 피드백을 보내는 사용자가 모임에 참여했는지 확인합니다.
        3. senderId와 receiverId가 동일한지 확인하여 자기 자신에게 피드백을 주는 것을 방지합니다.
        4. FeedbacksRepository의 existsByMeetingIdAndSenderId 메서드를 사용해 중복 피드백을 확인합니다.

        */
//        데이터 저장: 모든 유효성 검사를 통과하면 FeedbacksRepository의 save() 메서드를 호출하여 데이터를 저장합니다.

    }
}
