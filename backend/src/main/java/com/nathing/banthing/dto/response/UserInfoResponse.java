package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.TrustGrade;
import com.nathing.banthing.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String selfIntroduction;
    private String provider;
    private Integer trustScore;
    private TrustGrade trustGrade;
    private Integer noShowCount;
    private Boolean agree;

    /**
     * User 객체를 기반으로 UserInfoResponse 객체를 생성합니다.
     *
     * @param user User 객체로, 새로운 UserInfoResponse 객체 생성에 필요한 정보가 포함되어 있습니다.
     * @return UserInfoResponse 객체를 반환하며, User 객체의 정보를 기반으로 빌더 패턴을 통해 생성됩니다.
     *
     * @author - 강관주
     * @since - 2025-09-15
     */
    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .selfIntroduction(user.getSelfIntroduction())
                .provider(user.getProvider())
                .trustScore(user.getTrustScore())
                .trustGrade(user.getTrustGrade())
                .noShowCount(user.getNoShowCount())
                .agree(user.getAgree())
                .build();
    }
}
