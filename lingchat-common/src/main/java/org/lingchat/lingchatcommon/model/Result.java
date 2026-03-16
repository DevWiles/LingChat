package org.lingchat.lingchatcommon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;

    private String message;

    private T data;

    // 一个统一返回结果的快捷构造方法，用于在接口成功时快速生成 Result 对象。
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .data(data)
                .build();
    }

    // 一个统一返回结果的快捷构造方法，用于在接口失败时快速生成 Result 对象。
    public static <T> Result<T> fail(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    // 方法重载，方便调用，不用每次标注错误码，默认为 500 错误码。
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}
