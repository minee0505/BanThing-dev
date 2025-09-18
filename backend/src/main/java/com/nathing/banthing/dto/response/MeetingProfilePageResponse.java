package com.nathing.banthing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MeetingProfilePageResponse 클래스는 사용자가 참여한 모임 정보를 페이징 처리하여 응답으로 반환하기 위한 DTO(Data Transfer Object)입니다.
 *
 * 이 클래스는 모임 상세 정보 리스트, 페이지 번호, 페이지 크기, 총 요소 수와 같은 페이징 관련 정보를 포함합니다.
 * 주로 Controller 계층에서 클라이언트로 반환할 응답 객체로 사용됩니다.
 *
 * 주요 역할:
 * - 사용자가 참여한 모임의 세부 정보를 페이지 단위로 제공.
 * - 현재 페이지의 콘텐츠(모임 정보)와 전체 데이터에 대한 메타 정보를 포함.
 *
 * @author 강관주
 * @since 2025-09-18
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingProfilePageResponse {
    private List<MeetingDetailResponse> content;
    private int page;
    private int size;
    private long totalElements;
}
