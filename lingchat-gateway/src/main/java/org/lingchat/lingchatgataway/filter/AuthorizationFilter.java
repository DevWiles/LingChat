package org.lingchat.lingchatgataway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * 授权过滤器 - 基于角色的访问控制
 * 注意：认证（Authentication）由 AuthenticationFilter 完成
 *       授权（Authorization）由本过滤器完成
 */
@Component
public class AuthorizationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 获取用户 ID（由 AuthenticationFilter 注入）
        String userId = request.getHeaders().getFirst("X-User-Id");

        // 如果没有用户 ID，说明未经过认证（但路径可能是公开的）
        if (userId == null || userId.isEmpty()) {
            // 如果不是公开路径，则拒绝访问
            if (!isPublicPath(path)) {
                logger.warn("未认证用户尝试访问受保护资源 | 路径：{}", path);
                return onError(exchange, "未认证或令牌无效", HttpStatus.UNAUTHORIZED);
            }
            // 公开路径，放行
            return chain.filter(exchange);
        }

        // 基于路径的权限检查
        if (requiresAdminRole(path)) {
            // 检查用户角色（从请求头获取角色信息）
            String userRole = request.getHeaders().getFirst("X-User-Role");
            if (!"ADMIN".equals(userRole)) {
                logger.warn("用户权限不足，拒绝访问管理员路径 | userId: {} | 路径：{}", userId, path);
                return onError(exchange, "权限不足：需要管理员角色", HttpStatus.FORBIDDEN);
            }
        }

        // TODO: 可以扩展更多权限检查逻辑
        // 例如：从 auth-service 获取用户的详细权限信息
        // 或者检查用户是否被禁用等

        logger.debug("授权检查通过 | userId: {} | 路径：{}", userId, path);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -50; // 在认证过滤器（-100）之后执行
    }

    /**
     * 检查是否需要管理员角色
     */
    private boolean requiresAdminRole(String path) {
        return path.startsWith("/api/admin/") ||
                path.startsWith("/api/management/") ||
                path.contains("/admin/");
    }

    /**
     * 公开路径不需要授权检查
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/fallback/");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\":%d,\"message\":\"%s\"}", status.value(), message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
