package org.lingchat.lingchatgateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

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

        System.out.println("====== AuthFilter 接收到请求 ======");
        System.out.println("请求路径：" + path);
        System.out.println("请求方法：" + request.getMethod());
        System.out.println("完整 URL：" + request.getURI());

        // 放行认证相关接口（注册、登录等）- 支持两种路径格式
        if (path.startsWith("/auth/") || path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        System.out.println("Authorization header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        String token = authHeader.substring(7);

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
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            System.out.println("JWT 解析成功，userId: " + userId);

            // 将 userId 注入到下游请求 header 中
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            System.out.println("JWT 解析失败: " + e.getMessage());
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }
}
