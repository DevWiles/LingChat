package org.lingchat.authservice.service.impl;

import org.lingchat.authservice.dto.request.LoginRequest;
import org.lingchat.authservice.dto.request.RegisterRequest;
import org.lingchat.authservice.dto.response.UserResponse;
import org.lingchat.authservice.entity.User;
import org.lingchat.authservice.entity.UserProfile;
import org.lingchat.authservice.repository.UserRepository;
import org.lingchat.authservice.repository.UserProfileRepository;
import org.lingchat.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. 检查用户名是否存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建用户对象（只包含用户名和密码）
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 3. 保存用户信息到数据库
        User savedUser = userRepository.save(user);

        // 4. 创建用户 profile 信息
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(savedUser.getUser_id());
        userProfile.setNickname(request.getNickname());
        userProfile.setAvatar(request.getAvatar());

        // 5. 保存用户 profile 信息到数据库
        userProfileRepository.save(userProfile);

        // 6. 返回用户信息
        return convertToResponse(savedUser, userProfile);
    }

    @Override
    public UserResponse login(LoginRequest request) {
        // 1. 根据用户名查询用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 3. 查询用户 profile 信息
        UserProfile userProfile = userProfileRepository.findByUserId(user.getUser_id())
                .orElse(null);

        // 4. 返回用户信息
        return convertToResponse(user, userProfile);
    }

    @Override
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        UserProfile userProfile = userProfileRepository.findByUserId(user.getUser_id())
                .orElse(null);
        
        return convertToResponse(user, userProfile);
    }

    // 将 Entity 转化为 Response DTO
    private UserResponse convertToResponse(User user, UserProfile userProfile) {
        return UserResponse.builder()
                .id(user.getUser_id())
                .username(user.getUsername())
                .nickname(userProfile != null ? userProfile.getNickname() : null)
                .avatar(userProfile != null ? userProfile.getAvatar() : null)
                .status(user.getStatus())
                .createTime(userProfile != null ? userProfile.getCreateTime() : null)
                .build();
    }

}
