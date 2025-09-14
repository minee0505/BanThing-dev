package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.Meeting;
import lombok.Getter;

/**
 * MeetingCreateResponse 클래스는 클라이언트에게 모임 생성 결과를 응답으로 전달하기 위한 DTO(Data Transfer Object)입니다.
 * <p>
 * 이 클래스는 모임 생성 후, 생성된 모임의 ID 정보를 포함하고 있습니다.
 * 주로 Controller 계층에서 클라이언트로 반환할 응답 객체로 사용됩니다.
 * <p>
 * 주요 역할:
 * - Meeting 엔티티 객체에서 생성된 모임 ID를 추출하여 클라이언트에게 전달합니다.
 * - 데이터의 안정성과 불변성을 보장하기 위해 클래스는 불변(immutable)으로 설계되었습니다.
 * <p>
 * 생성자:
 * - `MeetingCreateResponse(Meeting meeting)`는 전달받은 `Meeting` 객체에서 ID를 추출하여 초기화합니다.
 *
 * @author - 고동현
 * @Since - 2025-09-15
 */
@Getter
public class MeetingCreateResponse {

    private final Long meetingId;

    public MeetingCreateResponse(Meeting meeting) {
        this.meetingId = meeting.getMeetingId();
    }
}