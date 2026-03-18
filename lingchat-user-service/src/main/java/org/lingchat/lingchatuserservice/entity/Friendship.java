package org.lingchat.lingchatuserservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "friendship", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "friendId"})
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long friendId;

    @Column(length = 50)
    private String remark;

    @Column(nullable = false)
    private Integer groupId;

    @Column(nullable = false)
    private Boolean isBlacklisted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
}