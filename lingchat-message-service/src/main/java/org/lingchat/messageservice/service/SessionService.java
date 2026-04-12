package org.lingchat.messageservice.service;

import java.util.Optional;

/**
 * 会话管理服务
 * 管理用户的 WebSocket 连接状态
 */
public interface SessionService {

    /**
     * 注册用户会话
     * @param userId 用户ID
     * @param channelId Netty Channel ID
     */
    void registerSession(Long userId, String channelId);

    /**
     * 移除用户会话
     * @param userId 用户ID
     */
    void removeSession(Long userId);

    /**
     * 根据用户ID获取 Channel ID
     * @param userId 用户ID
     * @return Channel ID，如果用户不在线则返回空
     */
    Optional<String> getChannelId(Long userId);

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isOnline(Long userId);

    /**
     * 根据 Channel ID 获取用户ID
     * @param channelId Channel ID
     * @return 用户ID
     */
    Optional<Long> getUserIdByChannelId(String channelId);
}
