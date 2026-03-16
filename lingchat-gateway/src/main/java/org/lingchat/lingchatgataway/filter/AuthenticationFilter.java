package org.lingchat.lingchatgataway.filter;

import org.lingchat.lingchatgataway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private AuthService authService;

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

        // 验证 token（包括 JWT 签名和黑名单检查）
        return authService.validateToken(token)
                .flatMap(userInfo -> {
                    // Token 有效，添加用户信息到请求头
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", userInfo.getUserId())
                            .header("X-Username", userInfo.getUsername())
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .switchIfEmpty(Mono.error(new RuntimeException("无效的认证令牌或令牌已失效")))
                .onErrorResume(e -> onError(exchange, "认证失败：" + e.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
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

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\":%d,\"message\":\"%s\"}", status.value(), message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}