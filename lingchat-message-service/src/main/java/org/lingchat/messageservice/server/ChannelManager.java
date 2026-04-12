package org.lingchat.messageservice.server;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 Netty Channel 与用户的映射关系
 */
@Component
public class ChannelManager {

    /**
     * Channel ID -> Channel 映射
     */
    private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    /**
     * Channel ID -> 用户ID 映射
     */
    private final Map<String, Long> channelToUserMap = new ConcurrentHashMap<>();

    /**
     * 用户ID -> Channel ID 映射
     */
    private final Map<Long, String> userToChannelMap = new ConcurrentHashMap<>();

    /**
     * 添加 Channel
     */
    public void addChannel(String channelId, Channel channel) {
        channelMap.put(channelId, channel);
    }

    /**
     * 绑定用户到 Channel
     */
    public void bindUser(String channelId, Long userId) {
        channelToUserMap.put(channelId, userId);
        userToChannelMap.put(userId, channelId);
    }

    /**
     * 移除 Channel
     */
    public void removeChannel(String channelId) {
        channelMap.remove(channelId);
        Long userId = channelToUserMap.remove(channelId);
        if (userId != null) {
            userToChannelMap.remove(userId);
        }
    }

    /**
     * 获取 Channel
     */
    public Optional<Channel> getChannel(String channelId) {
        return Optional.ofNullable(channelMap.get(channelId));
    }

    /**
     * 根据用户ID获取 Channel
     */
    public Optional<Channel> getChannelByUserId(Long userId) {
        String channelId = userToChannelMap.get(userId);
        if (channelId != null) {
            return getChannel(channelId);
        }
        return Optional.empty();
    }

    /**
     * 获取 Channel 对应的用户ID
     */
    public Optional<Long> getUserId(String channelId) {
        return Optional.ofNullable(channelToUserMap.get(channelId));
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return userToChannelMap.containsKey(userId);
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineCount() {
        return channelMap.size();
    }
}
