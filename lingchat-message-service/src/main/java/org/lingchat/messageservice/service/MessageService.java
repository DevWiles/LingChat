package org.lingchat.messageservice.service;

import io.netty.channel.Channel;
import org.lingchat.messageservice.dto.request.SendMsgRequest;
import org.lingchat.messageservice.entity.Message;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 发送消息
     * @param senderId 发送者ID
     * @param request 消息请求
     * @param channel 发送者的 Channel（用于返回 ACK）
     * @return 生成的消息
     */
    Message sendMessage(Long senderId, SendMsgRequest request, Channel channel);

    /**
     * 推送消息给在线用户
     * @param receiverId 接收者ID
     * @param message 消息
     */
    void pushMessage(Long receiverId, Message message);

    /**
     * 存储离线消息
     * @param receiverId 接收者ID
     * @param messageId 消息ID
     */
    void storeOfflineMessage(Long receiverId, Long messageId);

    /**
     * 获取用户的离线消息
     * @param userId 用户ID
     * @return 离线消息列表
     */
    List<Message> getOfflineMessages(Long userId);

    /**
     * 删除离线消息记录
     * @param userId 用户ID
     * @param messageId 消息ID
     */
    void removeOfflineMessage(Long userId, Long messageId);

    /**
     * 标记消息已读
     * @param userId 用户ID
     * @param messageId 消息ID
     */
    void markAsRead(Long userId, Long messageId);

    /**
     * 获取两个用户之间的聊天记录
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @return 消息列表
     */
    List<Message> getChatHistory(Long userId, Long friendId);

    /**
     * 推送离线消息给刚上线的用户
     * @param userId 用户ID
     * @param channel WebSocket Channel
     */
    void pushOfflineMessages(Long userId, Channel channel);
}
