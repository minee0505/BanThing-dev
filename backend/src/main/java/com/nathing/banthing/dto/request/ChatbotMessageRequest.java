package com.nathing.banthing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 김경민
 * @since 2025-09-12
 * 챗봇 메시지 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotMessageRequest {

    @NotBlank(message = "메시지는 필수입니다.")
    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다.")
    private String message;
}