package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbacksRepository extends JpaRepository<Feedback, Long> {
}
