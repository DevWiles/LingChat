package org.lingchat.lingchatuserservice.repository;

import org.lingchat.lingchatuserservice.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, Integer status);

    List<FriendRequest> findBySenderIdAndStatus(Long senderId, Integer status);
}
