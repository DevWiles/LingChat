package org.lingchat.messageservice.repository;

import org.lingchat.messageservice.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 查询两个用户之间的消息记录
     */
    List<Message> findBySenderIdAndReceiverIdOrderByCreateTimeAsc(Long senderId, Long receiverId);

    /**
     * 查询用户发送的消息
     */
    List<Message> findBySenderIdOrderByCreateTimeDesc(Long senderId);

    /**
     * 查询用户接收的消息
     */
    List<Message> findByReceiverIdOrderByCreateTimeDesc(Long receiverId);
}
