package org.lingchat.messageservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lingchat.lingchatcommon.utils.IdGenerator;
import org.lingchat.messageservice.dto.request.SendMsgRequest;
import org.lingchat.messageservice.dto.response.MessageResponse;
import org.lingchat.messageservice.entity.Message;
import org.lingchat.messageservice.entity.OfflineMessage;
import org.lingchat.messageservice.repository.MessageRepository;
import org.lingchat.messageservice.repository.OfflineMessageRepository;
import org.lingchat.messageservice.service.MessageService;
import org.lingchat.messageservice.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final OfflineMessageRepository offlineMessageRepository;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Message sendMessage(Long senderId, SendMsgRequest request, Channel channel) {
        // 生成消息ID（雪花算法）
        Long messageId = IdGenerator.nextId();

        // 创建消息实体
        Message message = new Message();
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setReceiverId(request.getTo());
        message.setType(request.getMsgType() != null ? request.getMsgType() : 1); // 默认文本消息
        message.setContent(request.getContent());
        message.setStatus(0); // 发送中

        // 保存消息到数据库
        messageRepository.save(message);
        log.info("消息已保存: msgId={}, from={}, to={}", messageId, senderId, request.getTo());

        // 更新消息状态为已送达
        message.setStatus(1);
        messageRepository.save(message);

        // 发送 ACK 给发送者
        sendAck(channel, messageId, 1);

        // 检查接收者是否在线
        if (sessionService.isOnline(request.getTo())) {
            // 在线则直接推送
            pushMessage(request.getTo(), message);
        } else {
            // 离线则存储离线消息
            storeOfflineMessage(request.getTo(), messageId);
            log.info("用户离线，存储离线消息: userId={}, msgId={}", request.getTo(), messageId);
        }

        return message;
    }

    @Override
    public void pushMessage(Long receiverId, Message message) {
        Optional<String> channelIdOpt = sessionService.getChannelId(receiverId);
        if (channelIdOpt.isEmpty()) {
            log.warn("无法推送消息，用户不在线: userId={}", receiverId);
            return;
        }

        // 这里需要从 ChannelManager 获取实际的 Channel 对象
        // 在后面的 Netty 实现中会补充这个逻辑
        log.info("推送消息给用户: userId={}, msgId={}", receiverId, message.getMessageId());
    }

    @Override
    @Transactional
    public void storeOfflineMessage(Long receiverId, Long messageId) {
        OfflineMessage offlineMessage = new OfflineMessage();
        offlineMessage.setUserId(receiverId);
        offlineMessage.setMessageId(messageId);
        offlineMessageRepository.save(offlineMessage);
    }

    @Override
    public List<Message> getOfflineMessages(Long userId) {
        List<OfflineMessage> offlineMessages = offlineMessageRepository.findByUserIdOrderByCreateTimeAsc(userId);
        List<Message> messages = new ArrayList<>();
        for (OfflineMessage om : offlineMessages) {
            messageRepository.findById(om.getMessageId()).ifPresent(messages::add);
        }
        return messages;
    }

    @Override
    @Transactional
    public void removeOfflineMessage(Long userId, Long messageId) {
        offlineMessageRepository.deleteByUserIdAndMessageId(userId, messageId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long messageId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            if (message.getReceiverId().equals(userId)) {
                message.setStatus(2); // 已读
                messageRepository.save(message);
            }
        });
    }

    @Override
    public List<Message> getChatHistory(Long userId, Long friendId) {
        List<Message> messages = new ArrayList<>();
        // 获取用户发送给好友的消息
        messages.addAll(messageRepository.findBySenderIdAndReceiverIdOrderByCreateTimeAsc(userId, friendId));
        // 获取好友发送给用户的消息
        messages.addAll(messageRepository.findBySenderIdAndReceiverIdOrderByCreateTimeAsc(friendId, userId));
        // 按时间排序
        messages.sort((m1, m2) -> m1.getCreateTime().compareTo(m2.getCreateTime()));
        return messages;
    }

    @Override
    public void pushOfflineMessages(Long userId, Channel channel) {
        List<Message> offlineMessages = getOfflineMessages(userId);
        if (offlineMessages.isEmpty()) {
            return;
        }

        log.info("推送离线消息: userId={}, count={}", userId, offlineMessages.size());
        for (Message message : offlineMessages) {
            MessageResponse response = MessageResponse.chat(
                    message.getMessageId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getType(),
                    message.getContent(),
                    message.getCreateTime()
            );
            sendMessageToChannel(channel, response);
        }
    }

    /**
     * 发送 ACK 确认给发送者
     */
    private void sendAck(Channel channel, Long msgId, Integer status) {
        if (channel == null || !channel.isActive()) {
            return;
        }
        MessageResponse ack = MessageResponse.ack(msgId, status);
        sendMessageToChannel(channel, ack);
    }

    /**
     * 发送消息到 Channel
     */
    private void sendMessageToChannel(Channel channel, MessageResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            channel.writeAndFlush(new TextWebSocketFrame(json));
        } catch (Exception e) {
            log.error("发送消息失败", e);
        }
    }
}
