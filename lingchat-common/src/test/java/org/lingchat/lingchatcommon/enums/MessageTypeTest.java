package org.lingchat.lingchatcommon.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTypeTest {

    @Test
    void testAllTypes_HaveValueAndDescription() {
        for (MessageType type : MessageType.values()) {
            assertNotNull(type.getType());
            assertNotNull(type.getDescription());
        }
    }

    @Test
    void testSpecificTypes() {
        assertEquals(1, MessageType.TEXT.getType());
        assertEquals("文本消息", MessageType.TEXT.getDescription());

        assertEquals(2, MessageType.IMAGE.getType());
        assertEquals(99, MessageType.SYSTEM.getType());
    }
}