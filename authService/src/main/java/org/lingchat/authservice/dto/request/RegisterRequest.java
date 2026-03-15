package org.lingchat.authservice.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 注册请求参数
 */
@Data
public class RegisterRequest {
    //用户名
    private String username;

    //密码
    private String password;

    //昵称
    private String nickname;

    //头像
    private String avatar;

}
