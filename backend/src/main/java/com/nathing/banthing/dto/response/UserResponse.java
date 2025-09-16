package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 @author user
 @since 25. 9. 15. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String nickname;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getNickname()
        );
    }
}