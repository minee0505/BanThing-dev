package com.nathing.banthing.controller;

import com.nathing.banthing.dto.common.ApiResponse;
import com.nathing.banthing.dto.response.UserInfoResponse;
import com.nathing.banthing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal String subject) {
        // subject는 OAuth2 nameAttributeKey(google: sub, kakao: id) → providerId로 사용 중
        UserInfoResponse dto = userService.findMeByProviderId(subject);

        ApiResponse<UserInfoResponse> apiResponse = ApiResponse.success("사용자의 정보가 성공적으로 조회되었습니다.", dto);

        return ResponseEntity.ok(apiResponse);
    }
}
