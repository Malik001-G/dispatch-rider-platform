package com.dispatchrider.platform.repository;

import com.dispatchrider.platform.entity.Role;
import com.dispatchrider.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    long countByRole(Role role);
}
