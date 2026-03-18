package org.lingchat.lingchatuserservice.service.impl;

import org.lingchat.lingchatuserservice.dto.request.FriendAddRequest;
import org.lingchat.lingchatuserservice.dto.request.FriendRemarkRequest;
import org.lingchat.lingchatuserservice.dto.response.FriendInfoResponse;
import org.lingchat.lingchatuserservice.dto.response.FriendRequestResponse;
import org.lingchat.lingchatuserservice.entity.FriendRequest;
import org.lingchat.lingchatuserservice.entity.Friendship;
import org.lingchat.lingchatuserservice.entity.UserProfile;
import org.lingchat.lingchatuserservice.repository.FriendRequestRepository;
import org.lingchat.lingchatuserservice.repository.FriendshipRepository;
import org.lingchat.lingchatuserservice.repository.UserProfileRepository;
import org.lingchat.lingchatuserservice.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public void sendFriendRequest(Long userId, FriendAddRequest request) {
        if (userId.equals(request.getFriendId())) {
            throw new RuntimeException("不能添加自己为好友");
        }

        if (friendshipRepository.existsByUserIdAndFriendId(userId, request.getFriendId())) {
            throw new RuntimeException("对方已经是你的好友");
        }

        FriendRequest existingRequest = friendRequestRepository.findAll()
                .stream()
                .filter(r -> r.getSenderId().equals(userId) && r.getReceiverId().equals(request.getFriendId()) && r.getStatus() == 0)
                .findFirst()
                .orElse(null);

        if (existingRequest != null) {
            throw new RuntimeException("已发送过好友申请，请勿重复发送");
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userId);
        friendRequest.setReceiverId(request.getFriendId());
        friendRequest.setMessage(request.getMessage());
        friendRequest.setStatus(0);

        friendRequestRepository.save(friendRequest);
    }

    @Override
    @Transactional
    public void handleFriendRequest(Long userId, Long requestId, Boolean agree) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("好友申请不存在"));

        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权处理该好友申请");
        }

        if (request.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }

        if (agree) {
            Friendship friendship1 = new Friendship();
            friendship1.setUserId(userId);
            friendship1.setFriendId(request.getSenderId());
            friendship1.setGroupId(0);
            friendship1.setIsBlacklisted(false);
            friendshipRepository.save(friendship1);

            Friendship friendship2 = new Friendship();
            friendship2.setUserId(request.getSenderId());
            friendship2.setFriendId(userId);
            friendship2.setGroupId(0);
            friendship2.setIsBlacklisted(false);
            friendshipRepository.save(friendship2);

            request.setStatus(1);
        } else {
            request.setStatus(2);
        }

        friendRequestRepository.save(request);
    }

    @Override
    public List<FriendInfoResponse> getFriendList(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserIdAndIsBlacklistedFalse(userId);

        List<FriendInfoResponse> result = new ArrayList<>();
        for (Friendship friendship : friendships) {
            UserProfile profile = userProfileRepository.findByUserId(friendship.getFriendId()).orElse(null);
            if (profile != null) {
                result.add(convertToFriendInfoResponse(profile, friendship.getRemark()));
            }
        }

        return result;
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));

        friendshipRepository.delete(friendship);

        Friendship reverseFriendship = friendshipRepository.findByUserIdAndFriendId(friendId, userId)
                .orElse(null);
        if (reverseFriendship != null) {
            friendshipRepository.delete(reverseFriendship);
        }
    }

    @Override
    @Transactional
    public void setFriendRemark(Long userId, FriendRemarkRequest request) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, request.getFriendId())
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));

        friendship.setRemark(request.getRemark());
        friendshipRepository.save(friendship);
    }

    @Override
    @Transactional
    public void addToBlacklist(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));

        friendship.setIsBlacklisted(true);
        friendshipRepository.save(friendship);
    }

    @Override
    @Transactional
    public void removeFromBlacklist(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));

        friendship.setIsBlacklisted(false);
        friendshipRepository.save(friendship);
    }

    @Override
    public List<FriendRequestResponse> getPendingRequests(Long userId) {
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(userId, 0);

        return requests.stream()
                .map(this::convertToFriendRequestResponse)
                .toList();
    }

    private FriendInfoResponse convertToFriendInfoResponse(UserProfile profile, String remark) {
        return FriendInfoResponse.builder()
                .userId(profile.getUserId())
                .nickname(profile.getNickName())
                .avatar(profile.getAvatar())
                .remark(remark)
                .status(profile.getStatus())
                .signature(profile.getSignature())
                .build();
    }

    private FriendRequestResponse convertToFriendRequestResponse(FriendRequest request) {
        UserProfile senderProfile = userProfileRepository.findByUserId(request.getSenderId()).orElse(null);

        return FriendRequestResponse.builder()
                .id(request.getId())
                .senderId(request.getSenderId())
                .senderNickname(senderProfile != null ? senderProfile.getNickName() : "未知用户")
                .message(request.getMessage())
                .status(request.getStatus())
                .createTime(request.getCreateTime())
                .build();
    }
}