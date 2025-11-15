package com.nathing.banthing.repository;

import com.nathing.banthing.entity.ChatbotConversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatbotConversationsRepository extends JpaRepository<ChatbotConversation, Long> {


    /**
     * 특정 사용자의 챗봇 대화 기록을 최신순으로 조회
     *
     * 기능: 로그인한 사용자가 과거에 챗봇과 나눈 대화 내역을 불러오는 메서드
     * - 챗봇창을 닫거나 로그아웃해도 대화 기록은 DB에 영구 저장됨
     * - 다시 로그인하면 이 메서드로 과거 대화를 조회할 수 있음
     * - 최신 대화부터 오래된 순으로 정렬하여 반환
     *
     * 페이징 처리 이유:
     * - 성능: 사용자가 수백 개의 대화를 했어도 필요한 만큼만 조회 (예: 최근 10개)
     * - UX: 사용자에게는 보통 최근 대화만 보여주면 충분함
     * - 메모리: 불필요한 데이터 로딩으로 인한 서버 부하 방지
     *
     * 주의사항: 현재 ddl-auto: create-drop 설정으로 서버 재시작 시 모든 데이터 삭제됨
     *
     * @param userId 조회할 사용자의 ID
     * @param pageable 페이징 설정 (예: 최근 10개만 가져오기)
     * @return 해당 사용자의 대화 기록 리스트 (최신순)
     */
    List<ChatbotConversation> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

}