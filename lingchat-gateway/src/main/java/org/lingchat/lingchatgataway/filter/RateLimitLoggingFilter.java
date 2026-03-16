package org.lingchat.lingchatgataway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 限流日志过滤器 - 记录被限流的请求
 * 配合 Spring Cloud Gateway 内置的 RequestRateLimiter 使用
 */
@Component
public class RateLimitLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .doOnEach(signal -> {
                    if (signal.isOnError()) {
                        Throwable ex = signal.getThrowable();
                        // 检查是否是限流异常
                        if (isRateLimitException(ex)) {
                            String ip = exchange.getRequest().getRemoteAddress() != null 
                                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                                    : "unknown";
                            String path = exchange.getRequest().getPath().value();
                            
                            logger.warn("⚠️ 请求被限流 | IP: {} | 路径：{} | 时间：{}", 
                                    ip, path, java.time.LocalDateTime.now());
                        }
                    }
                });
    }

    @Override
    public int getOrder() {
        return -200; // 高优先级，在认证之前执行
    }

    /**
     * 判断是否是限流异常
     */
    private boolean isRateLimitException(Throwable ex) {
        // Spring Cloud Gateway 的限流异常类名
        return ex.getClass().getName().contains("RequestRateLimiter") ||
                ex.getClass().getName().contains("DataBufferLimitException") ||
                (ex.getMessage() != null && ex.getMessage().contains("429"));
    }
}
