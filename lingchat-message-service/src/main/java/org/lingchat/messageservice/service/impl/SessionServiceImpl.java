package org.lingchat.messageservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lingchat.messageservice.service.SessionService;
import org.lingchat.lingchatcommon.constant.RedisKeyConstant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 会话过期时间（秒），用户无操作 24 小时后自动下线
     */
    private static final long SESSION_EXPIRE_SECONDS = 24 * 60 * 60;

    @Override
    public void registerSession(Long userId, String channelId) {
        String userSessionKey = String.format(RedisKeyConstant.USER_SESSION, userId);
        String sessionMapKey = String.format(RedisKeyConstant.USER_SESSION_MAP, channelId);

        // 存储用户ID到Channel ID的映射
        redisTemplate.opsForValue().set(userSessionKey, channelId, SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
        // 存储Channel ID到用户ID的映射（用于断连时清理）
        redisTemplate.opsForValue().set(sessionMapKey, userId.toString(), SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);

        log.info("用户会话注册成功: userId={}, channelId={}", userId, channelId);
    }

    @Override
    public void removeSession(Long userId) {
        String userSessionKey = String.format(RedisKeyConstant.USER_SESSION, userId);

        // 先获取 channelId，用于删除反向映射
        String channelId = redisTemplate.opsForValue().get(userSessionKey);

        // 删除用户到Channel的映射
        redisTemplate.delete(userSessionKey);

        // 删除Channel到用户的映射
        if (channelId != null) {
            String sessionMapKey = String.format(RedisKeyConstant.USER_SESSION_MAP, channelId);
            redisTemplate.delete(sessionMapKey);
        }

        log.info("用户会话移除成功: userId={}", userId);
    }

    @Override
    public Optional<String> getChannelId(Long userId) {
        String userSessionKey = String.format(RedisKeyConstant.USER_SESSION, userId);
        String channelId = redisTemplate.opsForValue().get(userSessionKey);
        return Optional.ofNullable(channelId);
    }

    @Override
    public boolean isOnline(Long userId) {
        String userSessionKey = String.format(RedisKeyConstant.USER_SESSION, userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(userSessionKey));
    }

    @Override
    public Optional<Long> getUserIdByChannelId(String channelId) {
        String sessionMapKey = String.format(RedisKeyConstant.USER_SESSION_MAP, channelId);
        String userIdStr = redisTemplate.opsForValue().get(sessionMapKey);
        if (userIdStr != null) {
            try {
                return Optional.of(Long.parseLong(userIdStr));
            } catch (NumberFormatException e) {
                log.warn("无法解析用户ID: {}", userIdStr);
            }
        }
        return Optional.empty();
    }
}
