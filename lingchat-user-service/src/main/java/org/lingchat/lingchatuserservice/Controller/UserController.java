package org.lingchat.lingchatuserservice.Controller;

import org.lingchat.lingchatcommon.model.Result;
import org.lingchat.lingchatuserservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.lingchatuserservice.dto.response.UserProfileResponse;
import org.lingchat.lingchatuserservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<Result<UserProfileResponse>> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(Result.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<Result<UserProfileResponse>> updateUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse profile = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(Result.success(profile));
    }

    @GetMapping("/search")
    public ResponseEntity<Result<UserProfileResponse>> searchUser(@RequestParam String username) {
        UserProfileResponse profile = userService.searchByUsername(username);
        return ResponseEntity.ok(Result.success(profile));
    }

}
