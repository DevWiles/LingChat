package org.lingchat.authservice.entity;

//JPA: Java 持久化操作接口
import jakarta.persistence.*;
//lombok: 简化 getter/setter 方法
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库用户表映射
 */
@Data // Lombok创建 getter/setter 方法，必须引用
@Entity // JPA实体类
@Table(name = "user") // 对应数据库中的 user 表
public class User {

    @Id // 主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 表示使用数据库自增策略
    private Long id;

    @Column(unique = true, nullable = false) // 唯一且不能为空
    private String username;

    @Column(nullable = false) // 不能为空
    private  String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 100)
    private String avatar;

    @Column(nullable = false, updatable = false) // 创建时间不能为空且不可更新
    private LocalDateTime createTime;

    @Column(nullable = false) // 更新时间
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        // 创建时间
        this.createTime = LocalDateTime.now();
        // 更新时间
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        // 更新时间
        this.updateTime = LocalDateTime.now();
    }

}
