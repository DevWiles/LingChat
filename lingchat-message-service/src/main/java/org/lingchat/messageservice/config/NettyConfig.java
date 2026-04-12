package org.lingchat.messageservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "netty.websocket")
public class NettyConfig {

    /**
     * WebSocket 服务端口
     */
    private int port = 8083;

    /**
     * WebSocket 路径
     */
    private String path = "/ws";
}
