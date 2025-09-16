package com.nathing.banthing.dto.response;

import lombok.Getter;
import java.util.List;

/**
 * ParticipantListResponse 클래스는 모임의 참가자 목록을 응답 객체로 제공하기 위한 DTO(Data Transfer Object)입니다.
 *
 * 이 클래스는 특정 모임에 대한 확정된 참가자와 대기 중인 신청자의 정보를 별도로 관리하며,
 * 클라이언트로 전달하기 적합한 구조로 데이터를 제공합니다.
 *
 * 주요 역할:
 * - 확정된 참가자와 대기 중인 신청자 리스트를 구분하여 저장 및 제공.
 * - API 응답 데이터의 일관성을 유지하고 가독성을 향상.
 *
 * 주요 필드:
 * - approved: 확정된 참가자 목록을 담고 있는 필드.
 * - pending: 대기 중인 신청자 목록을 담고 있는 필드.
 *
 * 생성자:
 * - ParticipantListResponse(List<MeetingParticipantResponse> approved, List<MeetingParticipantResponse> pending):
 *   각 필드를 전달받은 List를 통해 초기화하며, 각 리스트는 확정된 참가자와 대기 중인 신청자를 분리하여 저장.
 *
 * @author  고동현
 * @since 2025-09-16
 */
@Getter
public class ParticipantListResponse {
    private final List<MeetingParticipantResponse> approved; // 확정된 참가자 목록
    private final List<MeetingParticipantResponse> pending;  // 대기중인 신청자 목록

    public ParticipantListResponse(List<MeetingParticipantResponse> approved, List<MeetingParticipantResponse> pending) {
        this.approved = approved;
        this.pending = pending;
    }
}