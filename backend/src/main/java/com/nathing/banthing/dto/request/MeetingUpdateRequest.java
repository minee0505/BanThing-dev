package com.nathing.banthing.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MeetingUpdateRequest 클래스는 모임 수정 요청 데이터를 표현하는 DTO(Data Transfer Object)입니다.
 * 클라이언트가 모임 수정 요청 시 필요한 데이터를 이 클래스에 담아 서버로 전달합니다.
 *
 * 주요 역할:
 * - 클라이언트로부터 전달받은 모임 수정 요청 데이터를 캡슐화합니다.
 * - 필드마다 유효성 검증 애너테이션(@NotNull, @NotBlank 등)을 통해 입력 데이터의 유효성을 보장합니다.
 * - Controller 계층에서 Service 계층으로 데이터를 전달하는 용도로 사용됩니다.
 *
 * 필드 설명:
 * - martId: 수정된 모임이 연관된 마트의 고유 식별자. 필수 입력 값입니다.
 * - title: 수정된 모임 제목. 필수 입력 값이며 최대 100자를 초과할 수 없습니다.
 * - description: 수정된 모임의 본문 내용. 필수 입력 값입니다.
 * - meetingDate: 수정된 모임 날짜 및 시간. 필수 입력 값이며 현재 시간 이후여야 합니다.
 * - thumbnailImageUrl: 수정된 모임의 썸네일 이미지 URL. 선택적 입력 값입니다.
 *
 * 유효성 검증:
 * - martId는 null일 수 없습니다.
 * - title은 빈 문자열이나 공백일 수 없으며, 길이는 100자를 초과할 수 없습니다.
 * - description은 빈 문자열이나 공백일 수 없습니다.
 * - meetingDate는 null일 수 없으며, 반드시 현재 시간 이후여야 합니다.
 * - thumbnailImageUrl은 선택적 값으로, null이나 빈 문자열도 허용됩니다.
 *
 * @author 고동현
 * @since 2025-09-15
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUpdateRequest {

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

    private String thumbnailImageUrl;
}