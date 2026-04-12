package org.lingchat.messageservice.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import org.lingchat.messageservice.config.NettyConfig;
import org.lingchat.messageservice.handler.WebSocketHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyConfig nettyConfig;
    private final WebSocketHandler webSocketHandler;
    private final ChannelManager channelManager;

    /**
     * 读空闲超时时间（秒）
     */
    private static final int READ_IDLE_TIMEOUT = 120;

    /**
     * 写空闲超时时间（秒）
     */
    private static final int WRITE_IDLE_TIMEOUT = 0;

    /**
     * 全空闲超时时间（秒）
     */
    private static final int ALL_IDLE_TIMEOUT = 0;

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // HTTP 编解码器
        pipeline.addLast("http-codec", new HttpServerCodec());

        // 以块的方式写入
        pipeline.addLast("chunked-write", new ChunkedWriteHandler());

        // 聚合 HTTP 消息
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));

        // 心跳检测
        pipeline.addLast("idle-state", new IdleStateHandler(
                READ_IDLE_TIMEOUT, WRITE_IDLE_TIMEOUT, ALL_IDLE_TIMEOUT, TimeUnit.SECONDS));

        // WebSocket 协议处理器
        pipeline.addLast("websocket-protocol", new WebSocketServerProtocolHandler(nettyConfig.getPath()));

        // 自定义 WebSocket 处理器
        pipeline.addLast("websocket-handler", webSocketHandler);
    }
}
