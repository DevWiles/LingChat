package org.lingchat.lingchatcommon.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    void testBuilder_Pattern() {
        PageResult<String> result = PageResult.<String>builder()
                .list(Arrays.asList("a", "b", "c"))
                .total(100L)
                .pageNum(1)
                .pageSize(10)
                .totalPage(10)
                .build();

        assertEquals(3, result.getList().size());
        assertEquals(100L, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(10, result.getTotalPage());
    }

    @Test
    void testNoArgsConstructor() {
        PageResult<Void> result = new PageResult<>();
        assertNull(result.getList());
        assertNull(result.getTotal());
    }

    @Test
    void testAllArgsConstructor() {
        PageResult<String> result = new PageResult<>(
                Arrays.asList("item1", "item2"),
                50L,
                2,
                20,
                3
        );

        assertEquals(2, result.getList().size());
        assertEquals(50L, result.getTotal());
        assertEquals(2, result.getPageNum());
    }
}