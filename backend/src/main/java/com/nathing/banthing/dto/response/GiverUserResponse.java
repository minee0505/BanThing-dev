package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GiverUserResponse {
    private Long userId;
    private String nickname;

    // User 엔티티를 GiverUserResponse DTO로 변환하는 정적 팩토리 메서드
    public static GiverUserResponse from(User user) {
        return new GiverUserResponse(
                user.getUserId(),
                user.getNickname()
        );
    }
}