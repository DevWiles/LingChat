package org.lingchat.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户详细信息表映射
 */
@Data
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(unique = true, length = 50)
    private String username;

    @Column(length = 50)
    private String nickname;

    @Column(length = 100)
    private String avatar;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
