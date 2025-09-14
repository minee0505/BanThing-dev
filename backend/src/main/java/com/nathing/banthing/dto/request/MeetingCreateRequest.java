package com.nathing.banthing.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MeetingCreateRequest 클래스는 모임 생성 요청 데이터를 표현하는 DTO(Data Transfer Object)입니다.
 * 클라이언트가 모임 생성 요청 시 필요한 데이터를 이 클래스에 담아 서버로 전달합니다.
 * <p>
 * 주요 역할:
 * - 클라이언트 입력 데이터를 검증하기 위한 다양한 제약 조건(@NotNull, @NotBlank 등)을 포함하여 입력 데이터의 유효성을 보장합니다.
 * - Service 계층으로 전달되어 실제 모임 생성 로직에 사용됩니다.
 * <p>
 * 필드 설명:
 * - martId: 모임이 진행될 마트의 고유 식별자. 필수 값이며, null이면 안 됩니다.
 * - title: 모임 제목. 필수 값이며, 최대 100자까지 입력 가능합니다.
 * - description: 모임의 본문 설명. 필수 값입니다.
 * - meetingDate: 모임의 시작 날짜와 시간. 필수 값이며, 현재 시간 이후여야 합니다.
 * - maxParticipants: 모임의 최대 참여자 수. 필수 값입니다.
 * - thumbnailImageUrl: 모임의 썸네일 이미지 URL. 선택적 값입니다.
 * <p>
 * 유효성 검증:
 * - martId는 null이어서는 안 됩니다.
 * - title은 공백일 수 없으며, 최대 100자를 초과할 수 없습니다.
 * - description은 공백일 수 없습니다.
 * - meetingDate는 null이어서는 안 되며, 반드시 현재 시간 이후여야 합니다.
 * - maxParticipants는 null이어서는 안 됩니다.
 *
 * @author - 고동현
 *  @Since - 2025-09-15
 */
@Getter
@NoArgsConstructor
public class MeetingCreateRequest {

    @NotNull(message = "마트 ID는 필수입니다.")
    private Long martId;

    @NotBlank(message = "모임 제목은 필수입니다.")
    @Size(max = 100, message = "모임 제목은 100자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "본문 내용은 필수입니다.")
    private String description;

    @NotNull(message = "모임 시간은 필수입니다.")
    @Future(message = "모임 시간은 현재 시간 이후로 설정해야 합니다.")
    private LocalDateTime meetingDate;

    @NotNull(message = "최대 참여 인원은 필수입니다.")
    private Integer maxParticipants;

    private String thumbnailImageUrl;

}