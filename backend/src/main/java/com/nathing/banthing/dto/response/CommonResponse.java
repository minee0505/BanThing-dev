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

}