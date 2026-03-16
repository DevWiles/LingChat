package org.lingchat.lingchatcommon.exception;

import lombok.Getter;
import org.lingchat.lingchatcommon.enums.ErrorCode;

@Getter
public class BusinessException extends RuntimeException{

    private final Integer code;

    // 直接从枚举类中获取信息
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    // 从枚举类中获取信息，并拼接上自定义错误信息
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    // 自定义错误码和错误信息
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
