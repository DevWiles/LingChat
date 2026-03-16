package org.lingchat.lingchatcommon.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void testIsEmpty_Null() {
        assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    void testIsEmpty_EmptyString() {
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    void testIsEmpty_BlankString() {
        assertTrue(StringUtils.isEmpty("   "));
    }

    @Test
    void testIsEmpty_NotEmpty() {
        assertFalse(StringUtils.isEmpty("abc"));
        assertFalse(StringUtils.isEmpty("  hello  "));
    }

    @Test
    void testIsNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty("abc"));
    }

    @Test
    void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertFalse(StringUtils.isBlank("abc"));
    }

    @Test
    void testIsNotBlank() {
        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertTrue(StringUtils.isNotBlank("hello"));
    }

    @Test
    void testGenerateUUID() {
        String uuid1 = StringUtils.generateUUID();
        String uuid2 = StringUtils.generateUUID();

        assertNotNull(uuid1);
        assertEquals(32, uuid1.length());
        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void testSubstring_Normal() {
        String str = "Hello World";
        assertEquals("Hello", StringUtils.substring(str, 0, 5));
        assertEquals("World", StringUtils.substring(str, 6, 11));
    }

    @Test
    void testSubstring_Null() {
        assertNull(StringUtils.substring(null, 0, 5));
    }

    @Test
    void testSubstring_OutOfBounds() {
        String str = "Hi";
        assertEquals("Hi", StringUtils.substring(str, 0, 10));
        assertEquals("", StringUtils.substring(str, 5, 10));
    }

    @Test
    void testSubstring_NegativeIndex() {
        String str = "Hello";
        assertEquals("Hel", StringUtils.substring(str, 0, 3));
    }
}