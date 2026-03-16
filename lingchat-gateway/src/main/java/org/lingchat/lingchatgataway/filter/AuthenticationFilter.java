package org.lingchat.lingchatgataway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Value("${jwt.secret:LingChatSecretKey2026VeryLongAndSecure}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 放行公开路径
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 从请求头获取 token
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return onError(exchange, "未提供认证令牌", HttpStatus.UNAUTHORIZED);
        }

        // ✅ 简化：只检查 token 格式，详细验证交给 auth-service
        // 网关只负责传递 token，不负责验证
        String userId = extractUserIdFromToken(token);
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", userId != null ? userId : "unknown") // 提取用户 ID 传递给后端
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/fallback/");
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 从 Token 中提取用户 ID（不做详细验证）
     * 注意：真实验证由 auth-service 完成
     */
    private String extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            logger.debug("无法解析 Token 中的用户 ID: {}", e.getMessage());
            return null;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\":%d,\"message\":\"%s\"}", status.value(), message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
