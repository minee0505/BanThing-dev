package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Mart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MartsRepository extends JpaRepository<Mart, Long> {
}
