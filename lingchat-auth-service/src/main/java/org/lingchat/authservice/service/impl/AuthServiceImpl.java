package org.lingchat.authservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lingchat.authservice.dto.request.LoginRequest;
import org.lingchat.authservice.dto.request.RegisterRequest;
import org.lingchat.authservice.dto.response.UserResponse;
import org.lingchat.authservice.entity.User;
import org.lingchat.authservice.repository.UserRepository;
import org.lingchat.authservice.security.JwtTokenProvider;
import org.lingchat.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. 检查用户名是否存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建用户（只存认证信息：用户名 + 加密密码）
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        // 3. 调用 user-service 初始化用户档案（职责分离，profile 由 user-service 管理）
        try {
            Map<String, Object> profileRequest = new HashMap<>();
            profileRequest.put("userId", savedUser.getUser_id());
            profileRequest.put("username", savedUser.getUsername());
            profileRequest.put("nickname", request.getNickname());
            profileRequest.put("avatar", request.getAvatar());
            restTemplate.postForObject(userServiceUrl + "/api/user/profile/init", profileRequest, Void.class);
        } catch (Exception e) {
            // user-service 调用失败时记录日志，不影响注册主流程（profile 可延迟补偿）
            log.error("初始化用户档案失败，userId={}, 原因: {}", savedUser.getUser_id(), e.getMessage());
        }

        // 4. 返回用户信息
        return UserResponse.builder()
                .id(savedUser.getUser_id())
                .username(savedUser.getUsername())
                .nickname(request.getNickname())
                .avatar(request.getAvatar())
                .status(savedUser.getStatus())
                .build();
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

        // 3. 生成 JWT Token
        String token = jwtTokenProvider.generateToken(user.getUser_id(), user.getUsername());

        // 4. 返回用户信息和 token
        return UserResponse.builder()
                .id(user.getUser_id())
                .username(user.getUsername())
                .status(user.getStatus())
                .token(token)
                .build();
    }

    @Override
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return UserResponse.builder()
                .id(user.getUser_id())
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
    }
}

