我来分析一下 `lingchat-common` 模块在整个项目中的定位和应该实现的功能。

首先让我查看一下其他模块的结构和依赖关系，以便更好地理解 common 模块的职责。




**已查看文件** *pom.xml*

**已查看文件** *pom.xml*

**已查看文件** *pom.xml*

**已查看文件** *README.md*


现在让我查看一下 auth-service 中的一些核心代码，了解现有的业务逻辑：




**已查看文件** *User.java*

**已查看文件** *ApiResponse.java*

**已查看目录** *lingchat-common/src/main/java/org/lingchat/lingchatcommon* 1 个结果

**已查看文件** *LOG.md*


非常好！现在我对项目有了全面的了解。让我为你详细说说 `lingchat-common` 模块的设计和实施：

## 📋 lingchat-common 模块定位与设计方案

根据项目架构和 LOG.md 中的规划，`lingchat-common` 是整个 IM 系统的**共享核心模块**，所有微服务都会依赖它。

---

## 🎯 一、模块职责

**common 模块应该实现：**
1. **通用常量定义** - 系统全局常量
2. **枚举类管理** - 状态码、消息类型等
3. **统一异常体系** - 业务异常定义
4. **通用工具类** - 日期、字符串、加密等工具
5. **基础数据模型** - 通用的 DTO、VO、DO
6. **统一响应封装** - 结果集包装

---

## 📁 二、推荐目录结构

```
lingchat-common/src/main/java/org/lingchat/lingchatcommon/
├── constant/           # 常量类
│   ├── CommonConstant.java
│   └── RedisKeyConstant.java
│
├── enums/              # 枚举类
│   ├── ErrorCode.java
│   ├── MessageType.java
│   ├── UserStatusEnum.java
│   └── ResponseCodeEnum.java
│
├── exception/          # 异常类
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── AuthenticationException.java
│
├── utils/              # 工具类
│   ├── DateUtils.java
│   ├── StringUtils.java
│   └── IdGenerator.java
│
├── model/              # 通用数据模型
│   ├── Result.java
│   ├── PageResult.java
│   └── BaseEntity.java
│
└── annotation/         # 自定义注解（可选）
    └── RateLimit.java
```


---

## 💻 三、核心代码实现

### 1️⃣ **统一响应封装 - Result.java**

```java
package org.lingchat.lingchatcommon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    private Integer code;
    
    private String message;
    
    private T data;
    
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }
    
    public static <T> Result<T> success() {
        return success(null);
    }
    
    public static <T> Result<T> fail(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
    
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}
```


---

### 2️⃣ **错误码枚举 - ErrorCode.java**

```java
package org.lingchat.lingchatcommon.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
    SUCCESS(200, "操作成功"),
    
    // 通用错误 1xxx
    PARAM_ERROR(1001, "参数错误"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    
    // 认证相关 2xxx
    UNAUTHORIZED(2001, "未登录或 Token 已过期"),
    FORBIDDEN(2002, "无权限访问"),
    LOGIN_FAILED(2003, "登录失败"),
    TOKEN_INVALID(2004, "Token 无效"),
    
    // 用户相关 3xxx
    USER_NOT_EXIST(3001, "用户不存在"),
    USER_ALREADY_EXIST(3002, "用户已存在"),
    PASSWORD_ERROR(3003, "密码错误"),
    
    // 消息相关 4xxx
    MESSAGE_SEND_FAILED(4001, "消息发送失败"),
    MESSAGE_NOT_FOUND(4002, "消息不存在"),
    
    // 系统错误 5xxx
    SYSTEM_ERROR(5000, "系统内部错误"),
    SERVICE_UNAVAILABLE(5001, "服务不可用");
    
    private final Integer code;
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```


---

### 3️⃣ **业务异常类 - BusinessException.java**

```java
package org.lingchat.lingchatcommon.exception;

import lombok.Getter;
import org.lingchat.lingchatcommon.enums.ErrorCode;

@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
```


---

### 4️⃣ **消息类型枚举 - MessageType.java**

```java
package org.lingchat.lingchatcommon.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    
    TEXT(1, "文本消息"),
    IMAGE(2, "图片消息"),
    FILE(3, "文件消息"),
    VOICE(4, "语音消息"),
    VIDEO(5, "视频消息"),
    SYSTEM(99, "系统消息");
    
    private final Integer type;
    private final String description;
    
    MessageType(Integer type, String description) {
        this.type = type;
        this.description = description;
    }
}
```


---

### 5️⃣ **用户状态枚举 - UserStatusEnum.java**

```java
package org.lingchat.lingchatcommon.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    
    ONLINE(1, "在线"),
    OFFLINE(0, "离线"),
    AWAY(2, "离开"),
    DO_NOT_DISTURB(3, "勿扰"),
    INVISIBLE(4, "隐身");
    
    private final Integer status;
    private final String description;
    
    UserStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }
}
```


---

### 6️⃣ **Redis 键常量 - RedisKeyConstant.java**

```java
package org.lingchat.lingchatcommon.constant;

public class RedisKeyConstant {
    
    private static final String PREFIX = "lingchat:";
    
    // 用户相关
    public static final String USER_TOKEN = PREFIX + "user:token:%s";
    public static final String USER_INFO = PREFIX + "user:info:%s";
    public static final String USER_STATUS = PREFIX + "user:status:%s";
    
    // 会话相关
    public static final String USER_SESSION = PREFIX + "session:user:%s";
    public static final String SESSION_ID_MAP = PREFIX + "session:id:%s";
    
    // 消息相关
    public static final String UNREAD_COUNT = PREFIX + "message:unread:%s";
    public static final String MESSAGE_CACHE = PREFIX + "message:cache:%s";
    
    // 群组相关
    public static final String GROUP_MEMBERS = PREFIX + "group:members:%s";
    
    private RedisKeyConstant() {
        throw new IllegalStateException("Constant class");
    }
}
```


---

### 7️⃣ **分页结果封装 - PageResult.java**

```java
package org.lingchat.lingchatcommon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    private List<T> list;
    
    private Long total;
    
    private Integer pageNum;
    
    private Integer pageSize;
    
    private Integer totalPages;
}
```


---

### 8️⃣ **基础实体类 - BaseEntity.java**

```java
package org.lingchat.lingchatcommon.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class BaseEntity {
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }
    
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
```


---

### 9️⃣ **ID 生成器工具 - IdGenerator.java**

```java
package org.lingchat.lingchatcommon.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

public class IdGenerator {
    
    private static final long EPOCH = 1609459200000L; // 2021-01-01 00:00:00 UTC
    
    private static final long MACHINE_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    
    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
    
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    
    private static long machineId = 1L;
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;
    
    static {
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac != null) {
                machineId = ((mac[mac.length - 1] & 0xFF) << 8) | (mac[mac.length - 2] & 0xFF);
            }
        } catch (Exception e) {
            machineId = ManagementFactory.getRuntimeMXBean().getName().hashCode() & 0x1F;
        }
    }
    
    public static synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }
        
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }
    
    private static long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
    
    private IdGenerator() {
        throw new IllegalStateException("Utility class");
    }
}
```


---

### 🔟 **字符串工具类 - StringUtils.java**

```java
package org.lingchat.lingchatcommon.utils;

import java.util.UUID;

public class StringUtils {
    
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }
    
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }
        return str.substring(Math.min(start, str.length()), Math.min(end, str.length()));
    }
    
    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }
}
```


---

## 🔄 四、模块依赖关系

更新后的 pom.xml 应该保持简洁（你当前的已经很好了），其他模块这样依赖 common：

```xml
<!-- 在 auth-service、user-service 等模块的 pom.xml 中 -->
<dependency>
    <groupId>org.lingchat</groupId>
    <artifactId>lingchat-common</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```


---

## 📝 五、使用示例

在各服务中使用 common 模块的类：

```java
// Controller 层
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginRequest request) {
        User user = authService.login(request);
        return Result.success(UserVO.from(user));
    }
}

// Service 层
@Service
public class AuthServiceImpl implements AuthService {
    
    @Override
    public User login(LoginRequest request) {
        if (StringUtils.isEmpty(request.getUsername())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名不能为空");
        }
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        
        return user;
    }
}
```


---

## ✅ 六、开发建议

1. **保持轻量**：common 模块不应该依赖具体业务，只放通用代码
2. **避免循环依赖**：common 模块不能依赖其他 service 模块
3. **版本管理**：所有模块使用统一的版本号（父 pom 管理）
4. **文档注释**：公共类和方法要有完整的 JavaDoc
5. **单元测试**：工具类要编写充分的单元测试

---

需要我帮你创建这些文件吗？我可以逐个模块帮你生成代码。