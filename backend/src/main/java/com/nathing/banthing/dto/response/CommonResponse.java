package com.nathing.banthing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 @author user
 @since 25. 9. 15. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private String status;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>("success", message, data);
    }

    // 데이터가 없는 성공 응답을 위한 정적 팩토리 메서드 추가
    public static CommonResponse<Void> success(String message) {
        return new CommonResponse<>("success", message, null);
    }
}