package org.lingchat.lingchatcommon.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedisKeyConstantTest {

    @Test
    void testKeyFormat_ContainsPrefix() {
        assertTrue(RedisKeyConstant.USER_TOKEN.contains("lingchat:"));
        assertTrue(RedisKeyConstant.USER_INFO.contains("lingchat:"));
    }

    @Test
    void testKeyFormat_ContainsPlaceholder() {
        assertTrue(RedisKeyConstant.USER_TOKEN.contains("%s"));
        assertTrue(RedisKeyConstant.USER_INFO.contains("%s"));
    }

    @Test
    void testKeyUsage_Format() {
        String formattedKey = String.format(RedisKeyConstant.USER_TOKEN, "user123");
        assertEquals("lingchat:user:token:user123", formattedKey);
    }
}