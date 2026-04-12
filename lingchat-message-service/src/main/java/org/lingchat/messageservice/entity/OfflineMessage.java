package org.lingchat.messageservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "offline_message", indexes = {
    @Index(name = "idx_offline_user_id", columnList = "userId"),
    @Index(name = "idx_offline_create_time", columnList = "createTime")
})
public class OfflineMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 接收者用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 消息ID
     */
    @Column(nullable = false)
    private Long messageId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
}
