package org.lingchat.lingchatuserservice.service.impl;

import org.lingchat.lingchatcommon.enums.UserStatusEnum;
import org.lingchat.lingchatuserservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.lingchatuserservice.dto.response.UserProfileResponse;
import org.lingchat.lingchatuserservice.entity.UserProfile;
import org.lingchat.lingchatuserservice.repository.UserProfileRepository;
import org.lingchat.lingchatuserservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserProfileRepository userProfileRepository;

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
            profile.setNickName(request.getNickname());
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
            default -> throw new IllegalArgumentException("无效的状态码");
        };

        profile.setStatus(status);
        userProfileRepository.save(profile);
    }

    private UserProfileResponse convertToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .userId(profile.getUserId())
                .nickname(profile.getNickName())
                .avatar(profile.getAvatar())
                .signature(profile.getSignature())
                .status(profile.getStatus())
                .createTime(profile.getCreateTime())
                .build();
    }
}
