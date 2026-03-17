package org.lingchat.authservice.controller;

import org.lingchat.authservice.dto.request.LoginRequest;
import org.lingchat.authservice.dto.request.RegisterRequest;
import org.lingchat.authservice.dto.response.ApiResponse;
import org.lingchat.authservice.dto.response.UserResponse;
import org.lingchat.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*") // 允许跨域访问(开发环境，生产环境需要具体域名)
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    // @RequestBody: 表示接收 JSON 数据
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest request){
        UserResponse user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(user, "注册成功"));
    }

    /**
     * 登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest request){
        UserResponse user = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(user, "登录成功"));
    }

    /**
     * 根据用户名查询用户信息
     * GET /api/auth/username
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> findByUsername(@PathVariable String username){
        UserResponse user = authService.findByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user, "查询成功"));
    }

}
