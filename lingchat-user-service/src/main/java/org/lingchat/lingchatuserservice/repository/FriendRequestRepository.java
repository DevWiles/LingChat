package org.lingchat.lingchatuserservice.repository;

import org.lingchat.lingchatuserservice.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, Integer status);

    List<FriendRequest> findBySenderIdAndStatus(Long senderId, Integer status);

    /** 精确查询：替换原来的 findAll() + 内存过滤，避免全表扫描 */
    Optional<FriendRequest> findBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, Integer status);
}
