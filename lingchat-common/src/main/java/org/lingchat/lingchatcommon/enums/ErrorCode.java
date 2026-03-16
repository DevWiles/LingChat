package org.lingchat.lingchatcommon.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),

    // 通用错误
    PARAM_ERROR(1001, "参数错误"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_ALREADY_EXIST(1003, "数据已存在"),

    // 认证相关 2xxx
    UNAUTHORIZED(2001, "未登录或 Token 已过期"),
    FORBIDDEN(2002, "无权限访问"),
    LOGIN_FAIL(2003, "登录失败"),
    TOKEN_INVALID(2004, "Token 无效"),

    // 用户相关
    USER_NOT_EXIST(3001, "用户不存在"),
    USER_ALREADY_EXIST(3002, "用户已存在"),
    PASSWORD_ERROR(3003, "用户密码错误"),

    // 消息相关
    MESSAGE_SEND_FAILED(4001, "消息发送失败"),
    MESSAGE_NOT_FOUND(4002, "消息不存在"),

    // 系统错误
    SYSTEM_ERROR(5000, "系统错误"),
    SERVICE_UNAVAILABLE(5001, "服务不可用");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
