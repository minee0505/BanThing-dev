package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.MeetingParticipant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MeetingParticipantResponse 클래스는 클라이언트에게 모임 참여자 정보를 응답으로 전달하기 위한 DTO(Data Transfer Object)입니다.
 *
 * 이 클래스는 특정 모임의 개별 참여자 정보를 정형화된 형태로 제공하기 위해 설계되었습니다.
 * 주로 Controller 계층에서 API 응답으로 사용되며, 엔티티 객체인 MeetingParticipant를 기반으로 데이터를 생성합니다.
 *
 * 주요 역할:
 * - 특정 모임의 참여자 정보를 추출하여 클라이언트 요청에 적합한 형태로 전달합니다.
 * - 데이터를 캡슐화하여 응답 간의 데이터 일관성을 유지합니다.
 *
 * 필드 설명:
 * - userId: 참여자의 고유 사용자 ID.
 * - nickname: 참여자의 닉네임.
 * - profileImageUrl: 참여자의 프로필 이미지 URL.
 * - trusterScore: 참여자의 신뢰 점수.
 * - noShowCount: 참여자의 No-Show 발생 횟수.
 * - applicationStatus: 참여자의 모임 신청 상태 (예: PENDING, APPROVED 등).
 *
 * 생성자:
 * - MeetingParticipantResponse(MeetingParticipant participant):
 *   MeetingParticipant 엔티티 객체를 기반으로 필드를 초기화합니다.
 *   내부적으로 참여자(User) 객체의 정보를 통해 필요한 데이터를 추출합니다.
 * @author 고동현
 * @since 2025-09-15
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingParticipantResponse {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private int trusterScore;
    private int noShowCount;
    private String applicationStatus; // PENDING, APPROVED 등

    // 생성자나 from 메서드를 통해 엔티티를 DTO로 변환
    public MeetingParticipantResponse(MeetingParticipant participant) {
        this.userId = participant.getUser().getUserId();
        this.nickname = participant.getUser().getNickname();
        this.profileImageUrl = participant.getUser().getProfileImageUrl();
        this.trusterScore = participant.getUser().getTrustScore();
        this.noShowCount = participant.getUser().getNoShowCount();
        this.applicationStatus = participant.getApplicationStatus().name();
    }
}