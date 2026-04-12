package org.lingchat.messageservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    /**
     * 消息类型：chat, ack, pong
     */
    private String type;

    /**
     * 消息ID
     */
    private Long msgId;

    /**
     * 发送者用户ID
     */
    private Long from;

    /**
     * 接收者用户ID
     */
    private Long to;

    /**
     * 消息类型：1=文本, 2=图片等
     */
    private Integer msgType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息状态（用于 ACK 响应）
     */
    private Integer status;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * 创建聊天消息响应
     */
    public static MessageResponse chat(Long msgId, Long from, Long to, Integer msgType, String content, LocalDateTime time) {
        return MessageResponse.builder()
                .type("chat")
                .msgId(msgId)
                .from(from)
                .to(to)
                .msgType(msgType)
                .content(content)
                .time(time)
                .build();
    }

    /**
     * 创建 ACK 响应
     */
    public static MessageResponse ack(Long msgId, Integer status) {
        return MessageResponse.builder()
                .type("ack")
                .msgId(msgId)
                .status(status)
                .build();
    }

    /**
     * 创建心跳响应
     */
    public static MessageResponse pong() {
        return MessageResponse.builder()
                .type("pong")
                .build();
    }
}
