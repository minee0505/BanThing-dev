package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.Meeting;
import lombok.Getter;

/**
 * MeetingUpdateResponse 클래스는 모임 정보 수정 후 클라이언트에게 반환하기 위한 DTO(Data Transfer Object)입니다.
 *
 * 이 클래스는 수정된 모임의 ID를 포함하고 있으며, 데이터의 불변성을 보장하기 위해 불변(immutable) 객체로 설계되었습니다.
 * 주로 Controller 계층에서 사용되며, 수정 작업이 성공적으로 완료되었음을 나타내는 데이터를 클라이언트에 전달합니다.
 *
 * 주요 역할:
 * - 수정된 모임 ID를 클라이언트에게 전달합니다.
 * - 비즈니스 로직에서 생성한 수정 결과 값을 안전하게 감싸어 반환 데이터로 제공합니다.
 *
 * 생성자 설명:
 * - `MeetingUpdateResponse(Meeting meeting)` 생성자는 `Meeting` 엔티티에서 모임 ID를 추출하여 초기화합니다.
 *
 * 설계 의도:
 * - 객체 생성 이후 상태를 변경할 수 없도록 설계하여 데이터 일관성을 유지합니다.
 * - 수정 작업이 수행된 정확한 모임을 식별하기 위해 수정된 모임의 고유 ID만을 반환합니다.
 *
 * @author 고동현
 * @since 2025-09-15
 */
@Getter
public class MeetingUpdateResponse {

    private final Long updatedMeetingId;

    public MeetingUpdateResponse(Meeting meeting) {
        this.updatedMeetingId = meeting.getMeetingId();
    }
}