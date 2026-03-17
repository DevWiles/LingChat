package org.lingchat.lingchatgateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // 设置 JSON 响应格式
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String jsonResponse;
        
        // 根据异常类型设置不同的状态码和响应信息
        if (ex instanceof AccessDeniedException) {
            // 403 Forbidden - 权限不足
            response.setStatusCode(HttpStatus.FORBIDDEN);
            jsonResponse = "{\"code\": 403, \"message\": \"无权访问该接口，请先登录或检查权限配置\"}";
        } else if (ex instanceof ResponseStatusException) {
            // 处理带有状态码的异常
            ResponseStatusException rse = (ResponseStatusException) ex;
            response.setStatusCode(rse.getStatusCode());
            jsonResponse = "{\"code\": " + rse.getStatusCode().value() + ", \"message\": \"" + rse.getReason() + "\"}";
        } else {
            // 其他异常 - 500 Internal Server Error
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            jsonResponse = "{\"code\": 500, \"message\": \"网关内部错误，请稍后重试\"}";
        }
        
        // 将 JSON 字符串写入响应体
        DataBuffer buffer = response.bufferFactory()
                .wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
