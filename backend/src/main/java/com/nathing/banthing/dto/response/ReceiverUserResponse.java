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
public class ReceiverUserResponse {
    private Long userId;
    private String nickname;

    // User 엔티티에서 변환하는 팩토리 메서드
    public static ReceiverUserResponse from(User user) {
        return new ReceiverUserResponse(
                user.getUserId(),
                user.getNickname()
        );
    }
}