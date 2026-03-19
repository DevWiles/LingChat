package org.lingchat.lingchatgateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter {

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

        String token = request.getHeaders().getFirst("Authorization");
        System.out.println("token: " + token);

        if(token == null || !token.startsWith("Bearer ")){
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // TODO: 这里后续可以添加 token 验证逻辑（如 JWT 校验、Redis 黑名单检查等）

        return chain.filter(exchange);
    }
}
