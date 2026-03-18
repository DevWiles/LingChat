package org.lingchat.lingchatuserservice.repository;

import org.lingchat.lingchatuserservice.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByUserIdAndIsBlacklistedFalse(Long userId);

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    @Query("SELECT f FROM Friendship f WHERE f.userId = :userId AND f.friendId = :friendId OR f.userId = :friendId AND f.friendId = :userId")
    Optional<Friendship> findFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
