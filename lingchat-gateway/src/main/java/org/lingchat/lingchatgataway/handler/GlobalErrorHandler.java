package org.lingchat.lingchatgataway.handler;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局错误处理器
 * 捕获并处理所有未被处理的异常，返回统一的 JSON 格式错误响应
 */
@Component
@Order(-1) // 高优先级，确保能捕获到所有异常
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // 如果响应已提交，直接抛出异常
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应头为 JSON 格式
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 确定 HTTP 状态码
        HttpStatus status = determineHttpStatus(ex);
        response.setStatusCode(status);

        // 构建错误响应体
        String errorMessage = buildErrorMessage(ex);
        String body = String.format(
                "{\"code\":%d,\"message\":\"%s\",\"path\":\"%s\"}",
                status.value(),
                errorMessage,
                exchange.getRequest().getPath()
        );

        // 写入响应
        DataBuffer buffer = new DefaultDataBufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 根据异常类型确定合适的 HTTP 状态码
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException exception) {
            return HttpStatus.valueOf(exception.getStatusCode().value());
        }
        
        // 常见的异常类型映射
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        
        if (ex instanceof java.util.NoSuchElementException) {
            return HttpStatus.NOT_FOUND;
        }
        
        // 默认返回 500
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * 构建用户友好的错误消息
     */
    private String buildErrorMessage(Throwable ex) {
        // 生产环境应该记录详细日志，但返回给用户的消息要简洁
        String message = ex.getMessage();
        
        if (message == null || message.isEmpty()) {
            return "服务器内部错误";
        }
        
        // 避免暴露敏感信息
        if (ex instanceof ResponseStatusException) {
            return message;
        }
        
        // 对于其他异常，只返回通用消息（详细错误应记录到日志）
        return message;
    }
}
