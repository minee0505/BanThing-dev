package com.nathing.banthing.repository;

import com.nathing.banthing.entity.ChatbotConversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatbotConversationsRepository extends JpaRepository<ChatbotConversation, Long> {

    /**
     * 특정 사용자의 대화 기록을 최신순으로 조회 (Pageable로 개수 제한)
     */
    @Query("SELECT c FROM ChatbotConversation c " +
            "WHERE c.user.userId = :userId " +
            "ORDER BY c.createdAt DESC")
    List<ChatbotConversation> findRecentByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * 파생쿼리 + Pageable (User.userId 경로 명확화)
     */
    List<ChatbotConversation> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 의도 타입의 대화만 최신순 조회
     */
    List<ChatbotConversation> findByUser_UserIdAndIntentTypeOrderByCreatedAtDesc(
            Long userId, ChatbotConversation.IntentType intentType);
}