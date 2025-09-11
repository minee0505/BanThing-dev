package com.nathing.banthing.repository;

import com.nathing.banthing.entity.ChatbotConversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface chatbotConversationsRepository extends JpaRepository<ChatbotConversation, Long> {
}
