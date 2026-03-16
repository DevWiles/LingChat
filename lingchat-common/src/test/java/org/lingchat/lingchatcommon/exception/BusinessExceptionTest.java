package org.lingchat.lingchatcommon.exception;

import org.junit.jupiter.api.Test;
import org.lingchat.lingchatcommon.enums.ErrorCode;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void testConstructor_WithErrorCode() {
        BusinessException exception = new BusinessException(ErrorCode.USER_NOT_EXIST);

        assertEquals(3001, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testConstructor_WithErrorCodeAndCustomMessage() {
        BusinessException exception = new BusinessException(
                ErrorCode.PARAM_ERROR,
                "用户名不能为空"
        );

        assertEquals(1001, exception.getCode());
        assertEquals("用户名不能为空", exception.getMessage());
    }

    @Test
    void testConstructor_WithCodeAndMessage() {
        BusinessException exception = new BusinessException(9999, "自定义错误");

        assertEquals(9999, exception.getCode());
        assertEquals("自定义错误", exception.getMessage());
    }

    @Test
    void testThrowAndCatch() {
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException(ErrorCode.LOGIN_FAIL);
        });
    }

    @Test
    void testDifferentErrorCodes() {
        ErrorCode[] errorCodes = ErrorCode.values();

        for (ErrorCode errorCode : errorCodes) {
            BusinessException exception = new BusinessException(errorCode);
            assertNotNull(exception.getCode());
            assertNotNull(exception.getMessage());
        }
    }
}