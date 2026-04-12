package org.lingchat.messageservice.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lingchat.messageservice.config.NettyConfig;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettyWebSocketServer {

    private final NettyConfig nettyConfig;
    private final WebSocketChannelInitializer channelInitializer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childHandler(channelInitializer);

                ChannelFuture future = bootstrap.bind(nettyConfig.getPort()).sync();
                log.info("Netty WebSocket 服务启动成功，端口: {}, 路径: {}", nettyConfig.getPort(), nettyConfig.getPath());

                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Netty WebSocket 服务启动失败", e);
                Thread.currentThread().interrupt();
            } finally {
                destroy();
            }
        }, "netty-websocket-server").start();
    }

    @PreDestroy
    public void destroy() {
        log.info("关闭 Netty WebSocket 服务...");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
