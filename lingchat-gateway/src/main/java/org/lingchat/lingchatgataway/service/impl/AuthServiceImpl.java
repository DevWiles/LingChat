package org.lingchat.lingchatgataway.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.lingchat.lingchatgataway.model.UserInfo;
import org.lingchat.lingchatgataway.service.AuthService;
import org.lingchat.lingchatgataway.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    // Token 黑名单前缀（存储在 Redis 中）
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    
    // 刷新令牌宽限期（秒）- 用于防止并发问题
    private static final long REFRESH_GRACE_PERIOD = 60;

    /**
     * 验证 Token（只读访问 Redis 黑名单）
     */
    @Override
    public Mono<UserInfo> validateToken(String token) {
        return Mono.fromCallable(() -> jwtUtils.validateToken(token))
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.empty();
                    }
                    
                    String userId = jwtUtils.getUserIdFromToken(token);
                    
                    // 检查是否在黑名单中（只读操作）
                    return isTokenBlacklisted(token, userId)
                            .flatMap(isBlacklisted -> {
                                if (isBlacklisted) {
                                    return Mono.empty();
                                }
                                
                                UserInfo userInfo = new UserInfo();
                                userInfo.setUserId(userId);
                                userInfo.setUsername(jwtUtils.getUsernameFromToken(token));
                                return Mono.just(userInfo);
                            });
                })
                .onErrorResume(e -> {
                    // 降级策略：Redis 故障时，如果 JWT 有效则放行（记录日志）
                    System.err.println("Redis 访问失败，使用降级策略：" + e.getMessage());
                    try {
                        String userId = jwtUtils.getUserIdFromToken(token);
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUserId(userId);
                        userInfo.setUsername(jwtUtils.getUsernameFromToken(token));
                        return Mono.just(userInfo);
                    } catch (Exception ex) {
                        return Mono.empty();
                    }
                });
    }

    /**
     * 刷新 Token（需要写入 Redis 黑名单）
     */
    @Override
    public Mono<String> refreshToken(String token) {
        return Mono.fromCallable(() -> jwtUtils.refreshToken(token))
                .flatMap(newToken -> {
                    String userId = jwtUtils.getUserIdFromToken(newToken);
                    
                    // 将旧 token 加入黑名单（写操作）
                    return addToBlacklist(token, userId)
                            .thenReturn(newToken);
                });
    }
    
    /**
     * 检查 token 是否在黑名单中（只读）
     */
    private Mono<Boolean> isTokenBlacklisted(String token, String userId) {
        String blackListKey = BLACKLIST_PREFIX + userId;
        return redisTemplate.opsForSet().isMember(blackListKey, token)
                .defaultIfEmpty(false);
    }
    
    /**
     * 将 token 添加到黑名单
     */
    private Mono<Void> addToBlacklist(String token, String userId) {
        String blackListKey = BLACKLIST_PREFIX + userId;
        
        // 解析 token 获取过期时间
        Date expiryDate = getExpiryDateFromToken(token);
        long ttl = (expiryDate.getTime() - System.currentTimeMillis()) / 1000;
        
        if (ttl > 0) {
            return redisTemplate.opsForSet().add(blackListKey, token)
                    .then(redisTemplate.expire(blackListKey, java.time.Duration.ofSeconds(ttl + REFRESH_GRACE_PERIOD)))
                    .then();
        }
        return Mono.empty();
    }
    
    /**
     * 从 token 解析过期时间
     */
    private Date getExpiryDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtUtils.getSigningKeyPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }
}
