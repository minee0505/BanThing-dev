package com.nathing.banthing.repository;

import com.nathing.banthing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface usersRepository extends JpaRepository<User, Long> {
}
