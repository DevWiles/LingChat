package org.lingchat.lingchatuserservice.controller;

import org.lingchat.lingchatcommon.model.Result;
import org.lingchat.lingchatuserservice.dto.request.CreateProfileRequest;
import org.lingchat.lingchatuserservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.lingchatuserservice.dto.response.UserProfileResponse;
import org.lingchat.lingchatuserservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 内部接口：由 auth-service 在注册成功后调用，初始化用户档案
     * POST /api/user/profile/init
     */
    @PostMapping("/profile/init")
    public Result<Void> createProfile(@RequestBody CreateProfileRequest request) {
        userService.createProfile(request);
        return Result.success();
    }

    @GetMapping("/profile/{userId}")
    public Result<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }

    @PutMapping("/profile")
    public Result<UserProfileResponse> updateUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse profile = userService.updateUserProfile(userId, request);
        return Result.success(profile);
    }

    @GetMapping("/search")
    public Result<UserProfileResponse> searchUser(@RequestParam String username) {
        UserProfileResponse profile = userService.searchByUsername(username);
        return Result.success(profile);
    }

}

