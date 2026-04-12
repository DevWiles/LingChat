package org.lingchat.messageservice.controller;

import lombok.RequiredArgsConstructor;
import org.lingchat.lingchatcommon.model.Result;
import org.lingchat.messageservice.entity.Message;
import org.lingchat.messageservice.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取与好友的聊天记录
     */
    @GetMapping("/history/{friendId}")
    public Result<List<Message>> getChatHistory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long friendId) {
        List<Message> messages = messageService.getChatHistory(userId, friendId);
        return Result.success(messages);
    }

    /**
     * 获取离线消息
     */
    @GetMapping("/offline")
    public Result<List<Message>> getOfflineMessages(@RequestHeader("X-User-Id") Long userId) {
        List<Message> messages = messageService.getOfflineMessages(userId);
        return Result.success(messages);
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/read/{messageId}")
    public Result<Void> markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long messageId) {
        messageService.markAsRead(userId, messageId);
        return Result.success();
    }
}
