package org.lingchat.lingchatgataway.service;

import org.lingchat.lingchatgataway.model.UserInfo;
import reactor.core.publisher.Mono;

public interface AuthService {
    
    /**
     * 简单的 token 检查（仅格式检查，不做详细验证）
     * 注意：详细的 JWT 验证由 auth-service 完成
     * @param token JWT 令牌
     * @return 是否格式有效
     */
    boolean isTokenFormatValid(String token);
}
