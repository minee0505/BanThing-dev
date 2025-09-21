package com.nathing.banthing.repository;

import com.nathing.banthing.entity.ChatbotMeetingSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 챗봇 모임 추천 레포지토리
 * 챗봇이 사용자에게 추천한 모임 정보를 저장하고 관리하는 기능을 제공합니다.
 *
 * 주요 기능:
 * - 챗봇이 추천한 모임 정보 저장 (save)
 * - 추천 정보 조회 (findById)
 * - 추천 정보 삭제 (delete)
 *
 * 참고: 현재는 기본 CRUD 작업만 수행하며,
 * 추천 모임은 ChatbotServiceImpl에서 실시간으로 생성하여 저장합니다.
 *
 * @author 김경민
 * @since 2025-09-22
 */
public interface ChatbotMeetingsSuggestionRepository extends JpaRepository<ChatbotMeetingSuggestion, Long> {
}
