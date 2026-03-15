package org.lingchat.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    // 状态码
    private Integer code;

    // 消息
    private String message;

    // 数据
    private T data;

    // 成功响应
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    // 默认成功响应
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "操作成功");
    }

    // 失败响应
    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    // 默认失败响应
    public static <T> ApiResponse<T> fail(String message) {
        return fail(500, message);
    }
}



