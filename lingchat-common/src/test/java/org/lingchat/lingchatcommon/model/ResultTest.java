package org.lingchat.lingchatcommon.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void testSuccess_WithData() {
        String testData = "test data";
        Result<String> result = Result.success(testData);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(testData, result.getData());
    }

    @Test
    void testSuccess_NoData() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testSuccess_WithList() {
        List<String> list = Arrays.asList("a", "b", "c");
        Result<List<String>> result = Result.success(list);

        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().size());
    }

    @Test
    void testFail_WithCodeAndMessage() {
        Result<Void> result = Result.fail(404, "Not Found");

        assertEquals(404, result.getCode());
        assertEquals("Not Found", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testFail_WithMessage() {
        Result<Void> result = Result.fail("Error occurred");

        assertEquals(500, result.getCode());
        assertEquals("Error occurred", result.getMessage());
    }

    @Test
    void testSuccess_WithComplexObject() {
        TestVO vo = new TestVO("张三", 18);
        Result<TestVO> result = Result.success(vo);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("张三", result.getData().getName());
        assertEquals(18, result.getData().getAge());
    }

    static class TestVO {
        private String name;
        private Integer age;

        public TestVO(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public Integer getAge() { return age; }
    }
}