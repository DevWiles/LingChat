package org.lingchat.lingchatuserservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    private Long userId;

    @Column(unique = true, length = 50)
    private String username;

    @Column(length = 50)
    private String nickname;

    @Column(length = 200)
    private String avatar;

    @Column(length = 500)
    private String signature;

    /** 在线状态：0=离线 1=在线 2=离开 3=勿扰 4=隐身，对应 UserStatusEnum */
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer status = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;

}

