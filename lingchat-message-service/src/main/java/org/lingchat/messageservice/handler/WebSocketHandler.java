package org.lingchat.messageservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lingchat.messageservice.dto.request.SendMsgRequest;
import org.lingchat.messageservice.dto.response.MessageResponse;
import org.lingchat.messageservice.entity.Message;
import org.lingchat.messageservice.server.ChannelManager;
import org.lingchat.messageservice.service.MessageService;
import org.lingchat.messageservice.service.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Map;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    private final MessageService messageService;
    private final SessionService sessionService;
    private final ChannelManager channelManager;
    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf("userId");

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();
        channelManager.addChannel(channelId, ctx.channel());
        log.debug("Channel 激活: channelId={}", channelId);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();

        // 获取用户ID
        channelManager.getUserId(channelId).ifPresent(userId -> {
            // 移除会话
            sessionService.removeSession(userId);
            channelManager.removeChannel(channelId);
            log.info("用户断开连接: userId={}, channelId={}", userId, channelId);
        });

        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            // 处理 WebSocket 握手请求
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            // 处理 WebSocket 消息
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.warn("用户心跳超时，断开连接: channelId={}", ctx.channel().id().asLongText());
                ctx.close();
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket 异常: channelId={}", ctx.channel().id().asLongText(), cause);
        ctx.close();
    }

    /**
     * 处理 HTTP 请求（WebSocket 握手）
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        log.debug("收到 HTTP 请求: uri={}", uri);

        // 从 URL 参数中获取 token
        String token = getTokenFromUri(uri);
        if (token == null) {
            log.warn("WebSocket 连接缺少 token");
            ctx.close();
            return;
        }

        // 验证 token
        try {
            Key signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object userIdObj = claims.get("userId");
            if (userIdObj == null) {
                log.warn("JWT 中缺少 userId");
                ctx.close();
                return;
            }

            Long userId = Long.parseLong(userIdObj.toString());
            String channelId = ctx.channel().id().asLongText();

            // 绑定用户到 Channel
            ctx.channel().attr(USER_ID_KEY).set(userId);
            channelManager.bindUser(channelId, userId);
            sessionService.registerSession(userId, channelId);

            log.info("WebSocket 握手成功: userId={}, channelId={}", userId, channelId);

            // 推送离线消息
            messageService.pushOfflineMessages(userId, ctx.channel());

        } catch (Exception e) {
            log.warn("JWT 验证失败: {}", e.getMessage());
            ctx.close();
        }
    }

    /**
     * 处理 WebSocket 消息
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 处理关闭帧
        if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
            return;
        }

        // 处理 Ping 帧
        if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // 只处理文本帧
        if (!(frame instanceof TextWebSocketFrame)) {
            log.warn("不支持的消息类型: {}", frame.getClass().getName());
            return;
        }

        String text = ((TextWebSocketFrame) frame).text();
        log.debug("收到消息: {}", text);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> messageMap = objectMapper.readValue(text, Map.class);
            String type = (String) messageMap.get("type");

            if ("chat".equals(type)) {
                handleChatMessage(ctx, messageMap);
            } else if ("heartbeat".equals(type)) {
                handleHeartbeat(ctx);
            } else if ("ack".equals(type)) {
                handleAck(ctx, messageMap);
            } else {
                log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("消息解析失败: {}", text, e);
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(ChannelHandlerContext ctx, Map<String, Object> messageMap) {
        Long userId = ctx.channel().attr(USER_ID_KEY).get();
        if (userId == null) {
            log.warn("用户未认证");
            return;
        }

        SendMsgRequest request = SendMsgRequest.builder()
                .to(Long.parseLong(messageMap.get("to").toString()))
                .msgType(Integer.parseInt(messageMap.get("msgType").toString()))
                .content((String) messageMap.get("content"))
                .build();

        // 发送消息
        Message message = messageService.sendMessage(userId, request, ctx.channel());

        // 如果接收者在线，推送给接收者
        channelManager.getChannelByUserId(request.getTo()).ifPresent(receiverChannel -> {
            MessageResponse response = MessageResponse.chat(
                    message.getMessageId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getType(),
                    message.getContent(),
                    message.getCreateTime()
            );
            sendMessage(receiverChannel, response);
        });
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(ChannelHandlerContext ctx) {
        sendMessage(ctx, MessageResponse.pong());
    }

    /**
     * 处理 ACK
     */
    private void handleAck(ChannelHandlerContext ctx, Map<String, Object> messageMap) {
        Long userId = ctx.channel().attr(USER_ID_KEY).get();
        if (userId == null) {
            return;
        }

        Long msgId = Long.parseLong(messageMap.get("msgId").toString());
        messageService.markAsRead(userId, msgId);
        messageService.removeOfflineMessage(userId, msgId);
    }

    /**
     * 从 URI 中获取 token
     */
    private String getTokenFromUri(String uri) {
        try {
            URI uriObj = new URI(uri);
            String query = uriObj.getQuery();
            if (query == null) {
                return null;
            }

            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2 && "token".equals(pair[0])) {
                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.error("解析 URI 失败: {}", uri, e);
        }
        return null;
    }

    /**
     * 发送消息
     */
    private void sendMessage(ChannelHandlerContext ctx, MessageResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            ctx.writeAndFlush(new TextWebSocketFrame(json));
        } catch (Exception e) {
            log.error("发送消息失败", e);
        }
    }

    /**
     * 发送消息到 Channel
     */
    private void sendMessage(Channel channel, MessageResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            channel.writeAndFlush(new TextWebSocketFrame(json));
        } catch (Exception e) {
            log.error("发送消息失败", e);
        }
    }
}
