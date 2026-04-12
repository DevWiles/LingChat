package org.lingchat.lingchatgateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter {

    // auth-service 配置的 jwt.secret 是 Base64 字符串，但 JwtTokenProvider 直接对其做 .getBytes() 用作 HMAC key
    // 因此 Gateway 必须使用完全相同的字符串（即 Base64 原文），而非解码后的字节
    @Value("${jwt.secret:bGluZ2NoYXQtc2VjcmV0LWtleS1mb3Itand0LXRva2VuLWdlbmVyYXRpb24tdmVyeS1sb25nLXN0cmluZw==}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("AuthFilter 接收到请求: {} {}", request.getMethod(), path);

        // 放行认证相关接口（注册、登录等）- 支持两种路径格式
        if (path.startsWith("/auth/") || path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        // 检查是否是 WebSocket 握手请求
        String upgrade = request.getHeaders().getFirst(HttpHeaders.UPGRADE);
        boolean isWebSocket = "websocket".equalsIgnoreCase(upgrade);

        String token;
        if (isWebSocket) {
            // WebSocket 连接：从 URL 参数获取 token
            token = getTokenFromQuery(request.getURI().getQuery());
            if (token == null) {
                log.warn("WebSocket 连接缺少 token: {}", path);
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        } else {
            // 普通 HTTP 请求：从 Authorization Header 获取 token
            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("请求缺少有效的 Authorization header: {}", path);
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            token = authHeader.substring(7);
        }

        try {
            // 解析 JWT，获取 userId
            Key signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object userIdObj = claims.get("userId");
            String userId = userIdObj != null ? userIdObj.toString() : null;

            if (userId == null) {
                log.warn("JWT 中缺少 userId claim");
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            log.debug("JWT 验证通过，userId={}, path={}", userId, path);

            // 将 userId 注入到下游请求 header 中
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }

    /**
     * 从 URL 查询参数中提取 token
     */
    private String getTokenFromQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && "token".equals(pair[0])) {
                try {
                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}
