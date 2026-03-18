package org.lingchat.lingchatuserservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.lingchat.lingchatcommon.enums.UserStatusEnum;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    private Long userId;

    @Column(length = 50)
    private String nickName;

    @Column(length = 200)
    private String avatar;

    @Column(length = 500)
    private String signature;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private UserStatusEnum status = UserStatusEnum.OFFLINE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;

}
