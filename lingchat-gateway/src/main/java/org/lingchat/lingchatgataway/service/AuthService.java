package org.lingchat.lingchatgataway.service;

import org.lingchat.lingchatgataway.model.UserInfo;
import reactor.core.publisher.Mono;

public interface AuthService {
    
    /**
     * 验证 Token
     * @param token JWT 令牌
     * @return 用户信息（Mono 为空表示验证失败）
     */
    Mono<UserInfo> validateToken(String token);
    
    /**
     * 刷新 Token
     * @param token 旧令牌
     * @return 新令牌
     */
    Mono<String> refreshToken(String token);
}
