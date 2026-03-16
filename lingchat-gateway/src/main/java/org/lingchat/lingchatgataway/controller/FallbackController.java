package org.lingchat.lingchatgataway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 降级处理器
 * 当后端服务不可用或触发熔断时返回友好的错误提示
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * 认证服务降级处理
     * @return 降级响应
     */
    @RequestMapping("/auth")
    public Mono<ResponseEntity<String>> authFallback() {
        return Mono.just(ResponseEntity
                .status(503)
                .body("认证服务暂时不可用，请稍后重试"));
    }
}
