package com.mokaform.userservice.repository;

import com.mokaform.userservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsWithdraw(String email, Boolean isWithdraw);

    Boolean existsByEmail(String email);

    Boolean existsByNickname(String nickname);
}
