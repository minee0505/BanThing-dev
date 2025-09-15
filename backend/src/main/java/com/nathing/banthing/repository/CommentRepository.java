package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMeetingMeetingIdOrderByCreatedAtDesc(Long meetingId);
}