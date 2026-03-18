package org.lingchat.lingchatuserservice.service;

import org.lingchat.lingchatuserservice.dto.request.FriendAddRequest;
import org.lingchat.lingchatuserservice.dto.request.FriendRemarkRequest;
import org.lingchat.lingchatuserservice.dto.response.FriendInfoResponse;
import org.lingchat.lingchatuserservice.dto.response.FriendRequestResponse;

import java.util.List;

public interface FriendService {

    void sendFriendRequest(Long userId, FriendAddRequest request);

    void handleFriendRequest(Long userId, Long requestId, Boolean agree);

    List<FriendInfoResponse> getFriendList(Long userId);

    void deleteFriend(Long userId, Long friendId);

    void setFriendRemark(Long userId, FriendRemarkRequest request);

    void addToBlacklist(Long userId, Long friendId);

    void removeFromBlacklist(Long userId, Long friendId);

    List<FriendRequestResponse> getPendingRequests(Long userId);
}
