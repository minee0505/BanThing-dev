package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.Meeting;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * MeetingSimpleResponse 클래스는 클라이언트에게 특정 모임과 관련된 간략 정보를 응답하기 위한 DTO(Data Transfer Object)입니다.
 * <p>
 * 이 클래스는 주로 Controller 계층에서 클라이언트에게 반환되는 데이터 객체로 사용되며, 모임 엔티티의 주요 정보를 포함합니다.
 * 이를 통해 클라이언트는 모임의 기본적인 상태 및 정보를 조회할 수 있습니다.
 * <p>
 * 주요 역할:
 * - Meeting 엔티티 객체를 기반으로 필요한 간략 정보를 추출하여 초기화합니다.
 * - 모임 ID, 제목, 소속 마트 이름, 모임 일시, 현재 참여자 수, 최대 참여자 수, 상태, 썸네일 이미지 URL 등 주요 정보를 제공합니다.
 * - 데이터의 안정성과 불변성을 보장하기 위해 클래스는 불변(immutable)으로 설계되었습니다.
 * <p>
 * 생성자:
 * - `MeetingSimpleResponse(Meeting meeting)`는 전달받은 Meeting 객체를 기반으로 정보를 초기화합니다.
 * <p>
 * 필드 설명:
 * - meetingId: 모임의 고유 식별자
 * - title: 모임 제목
 * - martName: 모임이 소속된 마트의 이름
 * - meetingDate: 모임의 일시
 * - currentParticipants: 현재 모임에 참여하고 있는 인원 수
 * - maxParticipants: 모임의 최대 참여 가능 인원 수
 * - status: 모임의 현재 상태 (e.g., RECRUITING, FULL)
 * - thumbnailImageUrl: 모임 관련 썸네일 이미지의 URL
 *
 * @author - 고동현
 * @since - 2025-09-15
 */
@Getter
public class MeetingSimpleResponse {

    private final Long meetingId;
    private final Long martId;
    private final String title;
    private final String martName;
    private final LocalDateTime meetingDate;
    private final Integer currentParticipants;
    private final Integer maxParticipants;
    private final String status;
    private final String thumbnailImageUrl;
    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public MeetingSimpleResponse(Meeting meeting) {
        this.meetingId = meeting.getMeetingId();
        this.martId = meeting.getMart().getMartId();
        this.title = meeting.getTitle();
        this.martName = meeting.getMart().getMartName();
        this.meetingDate = meeting.getMeetingDate();
        this.currentParticipants = meeting.getCurrentParticipants();
        this.maxParticipants = meeting.getMaxParticipants();
        this.status = meeting.getStatus().name();
        this.thumbnailImageUrl = meeting.getThumbnailImageUrl();
        this.latitude = meeting.getMart().getLatitude();
        this.longitude = meeting.getMart().getLongitude();
    }
}