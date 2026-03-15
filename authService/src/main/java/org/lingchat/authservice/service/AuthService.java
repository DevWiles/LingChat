package org.lingchat.authservice.service;

import org.lingchat.authservice.dto.request.LoginRequest;
import org.lingchat.authservice.dto.request.RegisterRequest;
import org.lingchat.authservice.dto.response.UserResponse;

public interface AuthService {

    // 用户注册
    UserResponse register(RegisterRequest request);

    // 用户登录
    UserResponse login(LoginRequest request);

    // 根据用户名查询用户
    UserResponse findByUsername(String username);

}
