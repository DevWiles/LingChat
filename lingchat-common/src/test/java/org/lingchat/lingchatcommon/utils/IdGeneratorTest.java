package org.lingchat.lingchatcommon.utils;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

    @Test
    void testNextId_NotNull() {
        long id = IdGenerator.nextId();
        assertTrue(id > 0, "生成的 ID 应该大于 0");
    }

    @Test
    void testNextId_Uniqueness() throws Exception {
        Set<Long> ids = new HashSet<>();
        int size = 10000;

        for (int i = 0; i < size; i++) {
            ids.add(IdGenerator.nextId());
        }

        assertEquals(size, ids.size(), "生成的 ID 应该全部唯一");
    }

    @Test
    void testNextId_ConcurrentSafety() throws Exception {
        int threadCount = 10;
        int perThreadCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<Long> concurrentIds = ConcurrentHashMap.newKeySet();
        AtomicInteger errorCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < perThreadCount; j++) {
                        concurrentIds.add(IdGenerator.nextId());
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(0, errorCount.get(), "并发执行时不应出现异常");
        assertEquals(threadCount * perThreadCount, concurrentIds.size(),
                "并发生成的 ID 应该全部唯一");
    }

    @Test
    void testNextId_Increasing() {
        long id1 = IdGenerator.nextId();
        long id2 = IdGenerator.nextId();
        assertTrue(id2 > id1, "连续生成的 ID 应该递增");
    }

    @Test
    void testNextId_MultipleCalls() {
        long lastId = 0;
        for (int i = 0; i < 100; i++) {
            long currentId = IdGenerator.nextId();
            assertTrue(currentId > lastId, "第 " + i + " 次生成的 ID 应该递增");
            lastId = currentId;
        }
    }
}