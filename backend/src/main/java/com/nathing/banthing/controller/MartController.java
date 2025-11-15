package com.nathing.banthing.controller;

import com.nathing.banthing.dto.common.ApiResponse;
import com.nathing.banthing.dto.response.MartResponse;
import com.nathing.banthing.service.MartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/marts")
@RequiredArgsConstructor
public class MartController {

    private final MartService martService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MartResponse>>> getAllMarts() {
        List<MartResponse> marts = martService.findAllMarts();
        ApiResponse<List<MartResponse>> response = ApiResponse.success("전체 마트 목록이 성공적으로 조회되었습니다.", marts);
        return ResponseEntity.ok(response);
    }
}