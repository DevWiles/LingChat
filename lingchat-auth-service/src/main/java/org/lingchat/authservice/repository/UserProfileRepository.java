package org.lingchat.authservice.repository;

import org.lingchat.authservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    // 根据 userId 查询用户 profile
    Optional<UserProfile> findByUserId(Long userId);
}
