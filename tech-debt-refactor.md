# LingChat 技术债修复记录

## 问题一：GlobalExceptionHandler 注解缺失

**问题**：`auth-service` 的 `GlobalExceptionHandler` 类没有加 `@RestControllerAdvice`，导致所有 `@ExceptionHandler` 方法完全不生效，异常直接抛给客户端，返回 Spring 默认的错误格式。

**解决方案**：添加 `@RestControllerAdvice` 注解，同时将返回类型从自定义 `ApiResponse<T>` 统一改为 `lingchat-common` 的 `Result<T>`，消除重复的响应封装类。

**顺带发现**：`auth-service` 的 `pom.xml` 根本没有引入 `lingchat-common` 依赖，导致无法使用公共模块的任何类。一并补充了依赖。

**体现的设计模式**：
- **模板方法模式（Template Method）**：`@RestControllerAdvice` + `@ExceptionHandler` 是 Spring MVC 对模板方法模式的应用——框架定义了异常处理的调用时机，开发者只需填充具体处理逻辑。
- **外观模式（Facade）**：`Result<T>` 作为统一响应外观，屏蔽了内部异常细节，对外暴露一致的数据结构。

---

## 问题二：UserProfile 实体在两个服务中重复定义

**问题**：`auth-service` 和 `user-service` 各自定义了映射到同一张 `user_profile` 表的 JPA 实体，且字段不一致（auth-service 版本缺少 `signature` 字段，`avatar` 字段长度也不同）。两个服务同时启动时 Hibernate 的 `ddl-auto: update` 会产生 DDL 冲突，且职责边界模糊。

**解决方案**：
1. 删除 `auth-service` 中的 `UserProfile` 实体和 `UserProfileRepository`。
2. 在 `user-service` 新增内部接口 `POST /api/user/profile/init`，接收 `CreateProfileRequest`。
3. `auth-service` 注册成功后，通过 `RestTemplate` 调用该接口初始化用户档案。调用失败时只记录日志，不回滚注册事务（可后续补偿）。
4. 在 `auth-service` 的 `application.yaml` 中配置 `services.user-service.url`，为未来接入 Nacos 服务发现预留替换点。

**体现的设计模式**：
- **单一职责原则（SRP）**：用户认证信息（账号密码）归 `auth-service` 管，用户档案（昵称头像状态）归 `user-service` 管，职责边界清晰。
- **防腐层模式（Anti-Corruption Layer）**：`auth-service` 通过 `RestTemplate` + 配置化 URL 调用 `user-service`，隔离了两个服务的内部实现，未来切换为 Feign/Nacos 只需改配置。

---

## 问题三：updateOnlineStatus 状态永远不会写入

**问题**：`UserServiceImpl.updateOnlineStatus` 方法中，`UserStatusEnum` 枚举解析后赋值给了局部变量 `status`，但从未调用 `profile.setStatus()`，直接 `save(profile)` 保存的是原始数据。此外 `UserProfile` 实体本身也没有 `status` 字段，即便赋值也无法持久化。双重 Bug 导致在线状态功能完全失效。

**解决方案**：
1. 给 `UserProfile` 实体添加 `status` 字段（`INT DEFAULT 0`）。
2. 在方法中补全 `profile.setStatus(status.getStatus())` 赋值，再调用 `save()`。

**体现的设计模式**：
- **状态模式（State Pattern）**：`UserStatusEnum` 枚举封装了所有合法状态及其含义，`switch` 表达式做状态转换，避免了魔法数字散落在业务代码中。

---

## 问题四：FriendServiceImpl 全表查询

**问题**：`sendFriendRequest` 方法调用 `friendRequestRepository.findAll()` 加载全表数据到内存，再用 Stream 过滤出目标记录。随着好友申请数据增长，这会导致严重的性能问题和 OOM 风险。

**解决方案**：在 `FriendRequestRepository` 中添加 `findBySenderIdAndReceiverIdAndStatus(Long, Long, Integer)` 方法，由 Spring Data JPA 自动生成带 `WHERE` 条件的 SQL，将过滤下推到数据库。

**体现的设计模式**：
- **Repository 模式**：Spring Data JPA 的命名查询方法是 Repository 模式的典型实现——调用方只关心"查什么"，不关心"怎么查"，数据访问细节完全封装在 Repository 层。

---

## 问题五：TestRedis.java 临时测试类混入生产目录

**问题**：`lingchat-gateway` 模块的 `src/main/java` 下存在 `TestRedis.java`，使用硬编码的 `127.0.0.1:6379` 直连 Redis，会被打进生产 jar 包。

**解决方案**：直接删除该文件。测试代码应放在 `src/test/java` 下，或使用 Spring Boot Test 框架编写集成测试。

---

## 问题六：生产代码中大量 System.out.println

**问题**：`AuthFilter`、`JwtAuthenticationFilter`、`LoggingFilter` 共 12 处 `System.out.println`，在高并发场景下同步写标准输出会成为性能瓶颈，且无法按级别过滤、无法输出到日志文件、会泄露敏感信息（如完整 JWT token、Authorization header）。

**解决方案**：全部替换为 Lombok `@Slf4j` + SLF4J Logger，按语义选择日志级别：
- 正常请求流程 → `log.debug()`（生产环境默认不输出）
- 业务警告（token 缺失、解析失败）→ `log.warn()`
- 系统异常 → `log.error()`

**体现的设计模式**：
- **代理模式（Proxy）**：SLF4J 是日志门面，作为具体日志实现（Logback/Log4j2）的代理，调用方只依赖抽象接口，底层实现可随时替换。
- **策略模式（Strategy）**：不同日志级别对应不同的输出策略，运行时根据配置决定哪些日志实际输出。

---

## 问题七：Controller 包名大写（Java 命名规范违反）

**问题**：`user-service` 的 Controller 类放在 `org.lingchat.lingchatuserservice.Controller` 包下（首字母大写），违反 Java 包命名全小写的规范，在 Linux 文件系统（大小写敏感）上会导致编译失败或类找不到。

**解决方案**：使用 `git mv Controller ControllerTemp && git mv ControllerTemp controller` 两步重命名（Windows 文件系统大小写不敏感，直接 `git mv Controller controller` 无效），同步修改两个 Controller 文件的 `package` 声明。

**顺带优化**：将 `FriendController` 中冗余的 `ResponseEntity<Result<T>>` 包装简化为直接返回 `Result<T>`，与 `UserController` 风格统一。

---

## 整体收益

| 维度 | 修复前 | 修复后 |
|------|--------|--------|
| 异常处理 | 不生效，客户端收到 500 堆栈 | 统一 `Result<T>` 格式返回 |
| 服务职责 | auth/user 共同维护同一张表 | 职责分离，user-service 单一管理 profile |
| 在线状态 | 永远不会更新 | 正确持久化到 `user_profile.status` |
| 好友申请查询 | 全表扫描 O(n) | 索引精确查询 O(log n) |
| 日志 | 同步 stdout，泄露敏感信息 | SLF4J 分级，可配置，可过滤 |
| 包命名 | 大写 Controller，跨平台隐患 | 全小写，符合 Java 规范 |
