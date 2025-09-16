package com.nathing.banthing.service;

import com.nathing.banthing.dto.response.ChatbotConversationHistoryResponse;
import com.nathing.banthing.dto.response.ChatbotMessageResponse;

import java.util.List;

/**
 * @author 김경민
 * @since 2025-09-16
 * 반띵 AI 챗봇 서비스 인터페이스
 * 로그인 여부에 관계없이 챗봇 기능을 제공하며, 로그인 사용자에게는 개인화된 서비스 제공
 */

public interface ChatbotService {

    /**
     * 챗봇 서비스 상태 확인
     * Google Gemini API 연결 상태를 점검하여 서비스 가용성을 확인합니다.
     * 주로 헬스체크나 서비스 모니터링 용도로 사용됩니다.
     *
     * @return true: 서비스 정상, false: 서비스 장애
     */
    boolean healthCheck();

    /**
     * 로그인한 사용자의 챗봇 메시지 처리
     * 로그인한 사용자가 챗봇에게 메시지를 보낼 때 사용됩니다.
     * - 사용자별 개인화된 응답 제공 (닉네임 등 활용)
     * - 대화 내역을 데이터베이스에 영구 저장
     * - 사용자의 참여 이력을 바탕으로 맞춤형 모임 추천
     *
     * @param providerId 소셜 로그인 제공자 ID (카카오 등)
     * @param userMessage 사용자가 입력한 메시지
     * @return 챗봇 응답 (개인화된 답변 + 추천 모임 포함 가능)
     */
    ChatbotMessageResponse processAuthenticatedMessage(String providerId, String userMessage);

    /**
     * 게스트(비로그인) 사용자의 챗봇 메시지 처리
     * 로그인하지 않은 방문자가 챗봇을 사용할 때 호출됩니다.
     * - 기본적인 서비스 안내 및 일반적인 소분 가이드 제공
     * - 실시간 모임 정보 조회하여 현재 진행 중인 모임 안내
     * - 대화 내역 저장하지 않음 (일회성 응답)
     * - 회원가입 유도 메시지 포함
     *
     * @param userMessage 사용자가 입력한 메시지
     * @return 기본 챗봇 응답 (개인화 정보 없음)
     */
    ChatbotMessageResponse processGuestMessage(String userMessage);

    /**
     * 로그인 사용자의 챗봇 대화 기록 조회
     * 로그인한 사용자가 과거에 챗봇과 나눈 대화 내역을 불러옵니다.
     * - 최근 10개 대화를 최신순으로 반환
     * - 챗봇창을 다시 열 때 이전 대화 맥락 제공
     * - 사용자별로 독립적인 대화 기록 관리
     * - 페이징 처리로 성능 최적화
     *
     * @param providerId 소셜 로그인 제공자 ID
     * @return 사용자의 과거 대화 기록 목록 (최신순)
     */
    List<ChatbotConversationHistoryResponse> getChatHistory(String providerId);


}