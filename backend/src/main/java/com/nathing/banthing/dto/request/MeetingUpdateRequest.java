package com.nathing.banthing.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
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