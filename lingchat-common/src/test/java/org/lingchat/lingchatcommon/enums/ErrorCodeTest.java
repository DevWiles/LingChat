package org.lingchat.lingchatcommon.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {

    @Test
    void testAllEnums_HaveCodeAndMessage() {
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertNotNull(errorCode.getCode());
            assertNotNull(errorCode.getMessage());
            assertTrue(errorCode.getCode() > 0);
        }
    }

    @Test
    void testErrorCode_Ranges() {
        assertEquals(200, ErrorCode.SUCCESS.getCode());

        assertTrue(ErrorCode.PARAM_ERROR.getCode() >= 1000 &&
                ErrorCode.PARAM_ERROR.getCode() < 2000);
        assertTrue(ErrorCode.UNAUTHORIZED.getCode() >= 2000 &&
                ErrorCode.UNAUTHORIZED.getCode() < 3000);
        assertTrue(ErrorCode.USER_NOT_EXIST.getCode() >= 3000 &&
                ErrorCode.USER_NOT_EXIST.getCode() < 4000);
    }

    @Test
    void testGetCode() {
        assertEquals(1001, ErrorCode.PARAM_ERROR.getCode());
        assertEquals("参数错误", ErrorCode.PARAM_ERROR.getMessage());
    }
}