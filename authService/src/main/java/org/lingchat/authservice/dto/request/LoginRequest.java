package org.lingchat.authservice.dto.request;

import lombok.Data;

/**
 * 登录请求参数
 */
public class LoginRequest {
    //用户名
    private String username;

    //密码
    private String password;
}
