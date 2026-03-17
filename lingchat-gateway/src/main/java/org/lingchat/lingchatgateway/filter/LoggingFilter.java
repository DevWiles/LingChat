package org.lingchat.lingchatgateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long start = System.currentTimeMillis();

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long time = System.currentTimeMillis() - start;
                    System.out.println("请求耗时：" + time);
                })
        );
    }
}
