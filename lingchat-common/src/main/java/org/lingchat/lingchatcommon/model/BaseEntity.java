package org.lingchat.lingchatcommon.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
public class BaseEntity {

    /**
     * 本质上是一个 所有实体类共享的父类，作用是把公共字段 + 公共逻辑抽取出来复用，避免每个实体重复写代码。
     */

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private LocalDateTime updateTime;

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }

    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
