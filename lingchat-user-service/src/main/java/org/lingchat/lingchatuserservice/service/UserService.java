package org.lingchat.lingchatuserservice.service;


import org.lingchat.lingchatuserservice.dto.request.CreateProfileRequest;
import org.lingchat.lingchatuserservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.lingchatuserservice.dto.response.UserProfileResponse;

public interface UserService {

    /**
     * 创建用户档案（由 auth-service 在注册完成后调用）
     */
    void createProfile(CreateProfileRequest request);

    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);

    void updateOnlineStatus(Long userId, Integer statusCode);

    UserProfileResponse searchByUsername(String username);
}
