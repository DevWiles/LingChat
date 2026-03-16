package org.lingchat.lingchatcommon.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    @Test
    void testPrePersist_SetsCreateTimeAndUpdateTime() {
        TestEntity entity = new TestEntity();

        assertNull(entity.getCreateTime());
        assertNull(entity.getUpdateTime());

        entity.prePersist();

        assertNotNull(entity.getCreateTime());
        assertNotNull(entity.getUpdateTime());
        assertEquals(entity.getCreateTime(), entity.getUpdateTime());
    }

    @Test
    void testPreUpdate_UpdatesTime() throws InterruptedException {
        TestEntity entity = new TestEntity();
        entity.prePersist();

        LocalDateTime createTime = entity.getCreateTime();
        Thread.sleep(10);

        entity.preUpdate();

        assertTrue(entity.getUpdateTime().isAfter(createTime));
    }

    static class TestEntity extends BaseEntity {
    }
}