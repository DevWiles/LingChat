package org.lingchat.lingchatuserservice.controller;

import org.lingchat.lingchatcommon.model.Result;
import org.lingchat.lingchatuserservice.dto.request.FriendAddRequest;
import org.lingchat.lingchatuserservice.dto.request.FriendRemarkRequest;
import org.lingchat.lingchatuserservice.dto.response.FriendInfoResponse;
import org.lingchat.lingchatuserservice.dto.response.FriendRequestResponse;
import org.lingchat.lingchatuserservice.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @PostMapping("/request")
    public Result<String> sendFriendRequest(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody FriendAddRequest request) {
        friendService.sendFriendRequest(userId, request);
        return Result.success("好友申请已发送");
    }

    @PostMapping("/request/handle")
    public Result<String> handleFriendRequest(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long requestId,
            @RequestParam Boolean agree) {
        friendService.handleFriendRequest(userId, requestId, agree);
        return Result.success(agree ? "已同意好友申请" : "已拒绝好友申请");
    }

    @GetMapping("/list")
    public Result<List<FriendInfoResponse>> getFriendList(
            @RequestHeader("X-User-Id") Long userId) {
        List<FriendInfoResponse> friends = friendService.getFriendList(userId);
        return Result.success(friends);
    }

    @DeleteMapping("/{friendId}")
    public Result<String> deleteFriend(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long friendId) {
        friendService.deleteFriend(userId, friendId);
        return Result.success("已删除好友");
    }

    @PutMapping("/remark")
    public Result<String> setFriendRemark(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody FriendRemarkRequest request) {
        friendService.setFriendRemark(userId, request);
        return Result.success("备注已更新");
    }

    @PutMapping("/blacklist/{friendId}")
    public Result<String> addToBlacklist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long friendId) {
        friendService.addToBlacklist(userId, friendId);
        return Result.success("已加入黑名单");
    }

    @DeleteMapping("/blacklist/{friendId}")
    public Result<String> removeFromBlacklist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long friendId) {
        friendService.removeFromBlacklist(userId, friendId);
        return Result.success("已移出黑名单");
    }

    @GetMapping("/requests/pending")
    public Result<List<FriendRequestResponse>> getPendingRequests(
            @RequestHeader("X-User-Id") Long userId) {
        List<FriendRequestResponse> requests = friendService.getPendingRequests(userId);
        return Result.success(requests);
    }
}

