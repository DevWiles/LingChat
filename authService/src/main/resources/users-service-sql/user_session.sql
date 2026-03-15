-- 用户在线状态表
CREATE TABLE `user_session` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `connection_id` varchar(100) DEFAULT '' COMMENT '连接ID',
  `status` tinyint DEFAULT 1 COMMENT '1在线 0离线',
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `offline_time` datetime NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话表';