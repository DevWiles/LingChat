package org.lingchat.lingchatcommon.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserStatusEnumTest {

    @Test
    void testAllStatuses_HaveValueAndDescription() {
        for (UserStatusEnum status : UserStatusEnum.values()) {
            assertNotNull(status.getStatus());
            assertNotNull(status.getDescription());
        }
    }

    @Test
    void testOnlineStatus() {
        assertEquals(1, UserStatusEnum.ONLINE.getStatus());
        assertEquals("在线", UserStatusEnum.ONLINE.getDescription());
    }
}