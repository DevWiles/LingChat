package org.lingchat.lingchatuserservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lingchat.lingchatcommon.enums.UserStatusEnum;
import org.lingchat.lingchatuserservice.dto.request.CreateProfileRequest;
import org.lingchat.lingchatuserservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.lingchatuserservice.dto.response.UserProfileResponse;
import org.lingchat.lingchatuserservice.entity.UserProfile;
import org.lingchat.lingchatuserservice.repository.UserProfileRepository;
import org.lingchat.lingchatuserservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public void createProfile(CreateProfileRequest request) {
        if (userProfileRepository.existsById(request.getUserId())) {
            log.warn("用户档案已存在，跳过创建: userId={}", request.getUserId());
            return;
        }
        UserProfile profile = new UserProfile();
        profile.setUserId(request.getUserId());
        profile.setUsername(request.getUsername());
        profile.setNickname(request.getNickname());
        profile.setAvatar(request.getAvatar());
        userProfileRepository.save(profile);
        log.info("用户档案创建成功: userId={}", request.getUserId());
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户档案不存在"));

        return convertToResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(userId);
                    return userProfileRepository.save(newProfile);
                });

        if (request.getNickname() != null) {
            profile.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            profile.setAvatar(request.getAvatar());
        }
        if (request.getSignature() != null) {
            profile.setSignature(request.getSignature());
        }

        return convertToResponse(userProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public void updateOnlineStatus(Long userId, Integer statusCode) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户档案不存在"));

        UserStatusEnum status = switch (statusCode) {
            case 0 -> UserStatusEnum.OFFLINE;
            case 1 -> UserStatusEnum.ONLINE;
            case 2 -> UserStatusEnum.AWAY;
            case 3 -> UserStatusEnum.DO_NOT_DISTURB;
            case 4 -> UserStatusEnum.INVISIBLE;
            default -> throw new IllegalArgumentException("无效的状态码: " + statusCode);
        };

        profile.setStatus(status.getStatus());
        userProfileRepository.save(profile);
        log.info("用户在线状态已更新: userId={}, status={}", userId, status);
    }

    @Override
    public UserProfileResponse searchByUsername(String username) {
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToResponse(profile);
    }

    private UserProfileResponse convertToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .nickname(profile.getNickname())
                .avatar(profile.getAvatar())
                .signature(profile.getSignature())
                .createTime(profile.getCreateTime())
                .build();
    }
}

