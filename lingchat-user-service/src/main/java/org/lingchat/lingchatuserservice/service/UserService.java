package org.lingchat.lingchatuserservice.service;


import org.lingchat.lingchatuserservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.lingchatuserservice.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);

    void updateOnlineStatus(Long userId, Integer statusCode);
}
