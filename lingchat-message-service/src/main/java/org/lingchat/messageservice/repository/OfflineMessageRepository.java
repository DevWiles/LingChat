package org.lingchat.messageservice.repository;

import org.lingchat.messageservice.entity.OfflineMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfflineMessageRepository extends JpaRepository<OfflineMessage, Long> {

    /**
     * 查询用户的离线消息
     */
    List<OfflineMessage> findByUserIdOrderByCreateTimeAsc(Long userId);

    /**
     * 删除指定的离线消息
     */
    @Modifying
    @Query("DELETE FROM OfflineMessage o WHERE o.userId = ?1 AND o.messageId = ?2")
    void deleteByUserIdAndMessageId(Long userId, Long messageId);

    /**
     * 删除用户的所有离线消息
     */
    @Modifying
    @Query("DELETE FROM OfflineMessage o WHERE o.userId = ?1")
    void deleteByUserId(Long userId);

    /**
     * 统计用户离线消息数量
     */
    long countByUserId(Long userId);
}
