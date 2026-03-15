-- 用户表
CREATE TABLE `user` (
                        `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                        `username` varchar(50) NOT NULL UNIQUE COMMENT '账号',
                        `password` varchar(100) NOT NULL COMMENT '密码',
                        `nickname` varchar(50) DEFAULT '' COMMENT '昵称',
                        `avatar` varchar(255) DEFAULT '' COMMENT '头像',
                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
