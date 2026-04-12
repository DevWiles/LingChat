package org.lingchat.authservice.controller;

import org.lingchat.authservice.dto.request.LoginRequest;
import org.lingchat.authservice.dto.request.RegisterRequest;
import org.lingchat.authservice.dto.response.UserResponse;
import org.lingchat.authservice.service.AuthService;
import org.lingchat.lingchatcommon.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<UserResponse> register(@RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return Result.success(user);
    }

    /**
     * 登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<UserResponse> login(@RequestBody LoginRequest request) {
        UserResponse user = authService.login(request);
        return Result.success(user);
    }

    /**
     * 根据用户名查询用户信息
     * GET /api/auth/user/{username}
     */
    @GetMapping("/user/{username}")
    public Result<UserResponse> findByUsername(@PathVariable String username) {
        UserResponse user = authService.findByUsername(username);
        return Result.success(user);
    }

}

