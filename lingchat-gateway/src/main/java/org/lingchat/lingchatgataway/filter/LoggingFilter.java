package org.lingchat.lingchatgataway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * 访问日志过滤器
 * 记录每个请求的路径、方法、耗时、用户 ID、响应状态码等信息
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 记录请求开始时间
        Instant startTime = Instant.now();
        
        // 记录请求信息
        String path = request.getPath().value();
        String method = request.getMethod().name();
        String remoteAddress = request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
        
        logger.info("请求开始 | 方法：{} | 路径：{} | IP: {}", method, path, remoteAddress);
        
        // 执行请求并记录响应
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    // 计算耗时
                    long duration = Duration.between(startTime, Instant.now()).toMillis();
                    
                    // 获取响应状态码
                    int statusCode = exchange.getResponse().getStatusCode() != null 
                            ? exchange.getResponse().getStatusCode().value() 
                            : 0;
                    
                    // 获取用户 ID（如果有）
                    String userId = request.getHeaders().getFirst("X-User-Id");
                    
                    logger.info("请求完成 | 方法：{} | 路径：{} | 状态码：{} | 耗时：{}ms | 用户 ID: {}", 
                            method, path, statusCode, duration, userId != null ? userId : "anonymous");
                })
                .doOnError(throwable -> {
                    // 计算耗时
                    long duration = Duration.between(startTime, Instant.now()).toMillis();
                    
                    logger.error("请求失败 | 方法：{} | 路径：{} | 耗时：{}ms | 错误：{}", 
                            method, path, duration, throwable.getMessage());
                });
    }

    @Override
    public int getOrder() {
        return -200; // 比认证过滤器更高的优先级
    }
}
