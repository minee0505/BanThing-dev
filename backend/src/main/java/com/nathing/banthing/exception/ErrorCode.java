package com.nathing.banthing.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 고동현
 * @since 2025-09-11
 * 애플리케이션에서 발생하는 에러 코드들을 정의 하는 열거체
 * 에러상태코드, 에러메시지, 에러 이름 함께 관리합니다.
 *
 * 에러 코드 추가 시 이름 및 에러 코드 목적 작성 부탁드립니다.
 * ex) code: INTERNAL_SERVER_ERROR, 목적 : 서버 내부 오류 메시지용  - 이름
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 기본 에러 코드
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", 500),
    INVALID_INPUT("INVALID_INPUT", "입력값이 올바르지 않습니다.", 400),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", 404),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", 401),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", 403),

    // 비즈니스 에러 코드
    BUSINESS_ERROR("BUSINESS_ERROR", "비즈니스 로직 오류가 발생했습니다.", 400),
    VALIDATION_ERROR("VALIDATION_ERROR", "유효성 검사에 실패했습니다.", 400),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "이미 존재하는 리소스입니다.", 409),

    // 인증 관련 에러 코드
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404),
    DUPLICATE_USERNAME("DUPLICATE_USERNAME", "이미 사용 중인 사용자명입니다.", 409),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.", 409),
    INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호가 올바르지 않습니다.", 401),

    // 파일 관련 에러 코드
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "파일 크기가 제한을 초과했습니다.", 400),

    // 데이터베이스 관련 에러 코드
    DATA_INTEGRITY_VIOLATION("DATA_INTEGRITY_VIOLATION", "데이터 무결성 제약 조건을 위반했습니다.", 400),

    // 마트 관련 에러 코드
    MART_NOT_FOUND("MART_NOT_FOUND", "마트를 찾을 수 없습니다.", 404),
    TRIP_TITLE_ALREADY_EXISTS("TRIP_TITLE_ALREADY_EXISTS", "이미 존재하는 여행 제목입니다.", 409),
    TRIP_ACCESS_DENIED("TRIP_ACCESS_DENIED", "해당 여행에 접근할 권한이 없습니다.", 403),
    MEETING_NOT_FOUND("MEETING_NOT_FOUND", "모임을 찾을 수 없습니다.", 404),

    // 여행일지 관련 에러 코드
    TRAVEL_LOG_NOT_FOUND("TRAVEL_LOG_NOT_FOUND", "여행일지를 찾을 수 없습니다.", 404),
    TRAVEL_LOG_ACCESS_DENIED("TRAVEL_LOG_ACCESS_DENIED", "해당 여행일지에 접근할 권한이 없습니다.", 403),
    TRAVEL_LOG_TITLE_ALREADY_EXISTS("TRAVEL_LOG_TITLE_ALREADY_EXISTS", "이미 존재하는 여행일지 제목입니다.", 409),

    // 태그 관련 에러 코드
    HASHTAG_EXISTS("HASHTAG_EXISTS", "이미 존재하는 해시태그입니다.", 409)

    ;

    private final String code;
    private final String message;
    private final int status;


}
