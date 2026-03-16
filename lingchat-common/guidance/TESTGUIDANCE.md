好的，我为 `lingchat-common` 模块制定一份详尽的测试方案。

## 📋 测试方案总览

### 测试策略
- **单元测试为主** - 针对工具类、枚举、异常等
- **核心功能优先** - 优先测试 ID 生成器、响应封装等关键组件
- **分层覆盖** - 按重要程度分级测试

---

## 🎯 一、测试范围与优先级

### P0 - 必须测试（核心功能）

| 类名 | 测试内容 | 重要性 |
|------|---------|--------|
| `IdGenerator` | ID 唯一性、递增性、并发安全 | ⭐⭐⭐⭐⭐ |
| `Result` | 成功/失败响应封装、泛型支持 | ⭐⭐⭐⭐⭐ |
| `BusinessException` | 异常码映射、消息传递 | ⭐⭐⭐⭐ |
| `StringUtils` | 空值判断、截取等核心方法 | ⭐⭐⭐⭐ |

### P1 - 建议测试（常用功能）

| 类名 | 测试内容 | 重要性 |
|------|---------|--------|
| `PageResult` | 分页数据封装、页码计算 | ⭐⭐⭐ |
| `BaseEntity` | 时间戳自动填充 | ⭐⭐⭐ |
| `ErrorCode` | 枚举值正确性 | ⭐⭐⭐ |
| `MessageType` | 消息类型映射 | ⭐⭐ |
| `UserStatusEnum` | 状态值映射 | ⭐⭐ |

### P2 - 可选测试（简单常量）

| 类名 | 测试内容 | 重要性 |
|------|---------|--------|
| `RedisKeyConstant` | 常量格式验证 | ⭐ |
| 其他枚举类 | 基本构造 | ⭐ |

---

## 💻 二、详细测试用例设计

### 1️⃣ IdGeneratorTest.java（最高优先级）

```java
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
```


---

### 2️⃣ ResultTest.java（高优先级）

```java
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
```


---

### 3️⃣ BusinessExceptionTest.java（高优先级）

```java
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
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
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
```


---

### 4️⃣ StringUtilsTest.java（中高优先级）

```java
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
```


---

### 5️⃣ PageResultTest.java（中优先级）

```java
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
                .totalPages(10)
                .build();
        
        assertEquals(3, result.getList().size());
        assertEquals(100L, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(10, result.getTotalPages());
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
```


---

### 6️⃣ BaseEntityTest.java（中优先级）

```java
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
```


---

### 7️⃣ ErrorCodeTest.java（中优先级）

```java
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
```


---

### 8️⃣ MessageTypeTest.java（低优先级）

```java
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
```


---

### 9️⃣ UserStatusEnumTest.java（低优先级）

```java
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
```


---

### 🔟 RedisKeyConstantTest.java（可选）

```java
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
```


---

## 📊 三、测试执行计划

### 阶段一：核心功能测试（必须完成）

```bash
# 运行核心测试
mvn test -Dtest=IdGeneratorTest
mvn test -Dtest=ResultTest
mvn test -Dtest=BusinessExceptionTest
mvn test -Dtest=StringUtilsTest
```


**预期结果**：
- ✅ 所有测试通过
- ✅ 代码覆盖率 > 80%（核心类）

### 阶段二：常用功能测试（建议完成）

```bash
# 运行次级测试
mvn test -Dtest=PageResultTest
mvn test -Dtest=BaseEntityTest
mvn test -Dtest=ErrorCodeTest
```


**预期结果**：
- ✅ 所有测试通过
- ✅ 代码覆盖率 > 60%

### 阶段三：完整测试（可选）

```bash
# 运行所有测试
mvn test
```


**预期结果**：
- ✅ 所有测试通过
- ✅ 总体代码覆盖率 > 70%

---

## 📈 四、代码覆盖率要求

使用 JaCoCo 进行覆盖率统计：

### 添加 JaCoCo 插件到 pom.xml

```xml
<build>
    <plugins>
        <!-- ... existing plugins ... -->
        
        <!-- JaCoCo 代码覆盖率 -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```


### 生成覆盖率报告

```bash
# 生成 HTML 报告
mvn clean test jacoco:report

# 查看报告
# 打开 target/site/jacoco/index.html
```


### 覆盖率标准

| 模块类型 | 行覆盖率 | 分支覆盖率 | 方法覆盖率 |
|---------|---------|-----------|-----------|
| 工具类（IdGenerator） | ≥ 90% | ≥ 85% | ≥ 95% |
| 模型类（Result） | ≥ 80% | ≥ 75% | ≥ 90% |
| 枚举类 | ≥ 70% | ≥ 60% | ≥ 80% |
| 常量类 | ≥ 50% | - | ≥ 50% |

---

## 🚀 五、持续集成建议

### GitHub Actions 示例

```yaml
name: Java CI with Maven

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: mvn clean install
    
    - name: Run tests
      run: mvn test
    
    - name: Generate JaCoCo report
      run: mvn jacoco:report
    
    - name: Upload coverage reports
      uses: codecov/codecov-action@v3
      with:
        files: ./target/site/jacoco/jacoco.xml
```


---

## 📝 六、测试文件目录结构

创建后的测试文件结构：

```
src/test/java/org/lingchat/lingchatcommon/
├── utils/
│   ├── IdGeneratorTest.java          ⭐⭐⭐⭐⭐
│   └── StringUtilsTest.java          ⭐⭐⭐⭐
├── model/
│   ├── ResultTest.java               ⭐⭐⭐⭐⭐
│   ├── PageResultTest.java           ⭐⭐⭐
│   └── BaseEntityTest.java           ⭐⭐⭐
├── exception/
│   └── BusinessExceptionTest.java    ⭐⭐⭐⭐
├── enums/
│   ├── ErrorCodeTest.java            ⭐⭐⭐
│   ├── MessageTypeTest.java          ⭐⭐
│   └── UserStatusEnumTest.java       ⭐⭐
└── constant/
    └── RedisKeyConstantTest.java     ⭐
```


---

## 💡 七、快速开始（推荐顺序）

### 第一批（今天完成）：
1. ✅ `IdGeneratorTest` - 最重要
2. ✅ `ResultTest` - 最常用
3. ✅ `StringUtilsTest` - 基础工具

### 第二批（明天完成）：
4. ✅ `BusinessExceptionTest`
5. ✅ `PageResultTest`
6. ✅ `ErrorCodeTest`

### 第三批（有时间再做）：
7. ⭕ `BaseEntityTest`
8. ⭕ 其他枚举类测试
9. ⭕ 常量类测试

---

## 🎯 总结

这份测试方案的特点：

1. **分级明确** - P0/P1/P2 三个优先级
2. **详略得当** - 核心功能详细测试，简单功能简化测试
3. **可执行性强** - 提供完整代码，复制即可使用
4. **覆盖率要求** - 有明确的量化指标
5. **渐进式推进** - 分批次完成，不会一次性压力太大

**建议**：先从 P0 级别的 3-4 个测试开始，保证核心功能的可靠性，然后逐步扩展到其他模块。