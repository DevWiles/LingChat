package org.lingchat.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应（不包含密码等敏感信息）
 */
@Data
@Builder
public class UserResponse {

    // 用户ID
    private Long id;

    // 用户名
    private String username;

    // 昵称
    private String nickname;

    // 头像
    private String avatar;

    // 用户状态：0-禁用，1-正常
    private Integer status;

    // 创建时间
    private LocalDateTime createTime;

    // JWT
    private String token;
}
