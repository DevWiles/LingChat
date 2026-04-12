package org.lingchat.messageservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "message", indexes = {
    @Index(name = "idx_sender_id", columnList = "senderId"),
    @Index(name = "idx_receiver_id", columnList = "receiverId"),
    @Index(name = "idx_create_time", columnList = "createTime")
})
public class Message {

    @Id
    @Column(name = "message_id")
    private Long messageId;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private Integer type;

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 消息状态：0=发送中, 1=已送达, 2=已读, 3=撤回
     */
    @Column(nullable = false)
    private Integer status = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
}
