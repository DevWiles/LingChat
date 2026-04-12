# LingChat — Claude 开发指南

本文件为 Claude Code 提供项目上下文，帮助 AI 快速理解代码库结构、开发规范和当前进展，以便给出准确、一致的协助。

---

## 项目简介

**灵雀轻聊（LingChat）** 是一个 Java 学习/求职展示项目，实现了一套基于 Spring Cloud 微服务架构的即时通讯（IM）系统后端，并计划配套 React 前端。

- **开发语言**：Java 21（后端）、TypeScript（前端，规划中）
- **构建工具**：Maven（多模块父子项目）
- **核心框架**：Spring Boot 3.2.5 + Spring Cloud 2023.0.1
- **项目根目录**：`f:/JavaCode/LingChat/`（Windows 路径）

---

## 模块结构

```
LingChat/                         ← Maven 父工程（只有 pom.xml，不写业务代码）
├── lingchat-common/              ← 公共库（被其他服务 Maven 依赖）✅
├── lingchat-auth-service/        ← 认证服务 :8081 ✅
├── lingchat-gateway/             ← API 网关 :8080 ✅
├── lingchat-user-service/        ← 用户/好友服务 :8082 ✅
├── lingchat-message-service/     ← 消息服务 :8083 ❌ 待创建
├── lingchat-group-service/       ← 群聊服务 :8084 ❌ 待创建
├── lingchat-presence-service/    ← 在线状态服务 :8085 ❌ 待创建
└── guidance/                     ← AI 对话历史记录（仅供参考，非代码）
```

### 各模块包名

| 模块 | 根包名 |
|------|--------|
| lingchat-common | `org.lingchat.lingchatcommon` |
| lingchat-auth-service | `org.lingchat.authservice` |
| lingchat-gateway | `org.lingchat.lingchatgateway` |
| lingchat-user-service | `org.lingchat.lingchatuserservice` |

---

## 技术栈速查

| 技术 | 用途 | 版本 |
|------|------|------|
| Spring Boot | 各微服务基础框架 | 3.2.5 |
| Spring Cloud Gateway | API 网关（WebFlux 响应式） | — |
| Spring Security | 认证授权 | — |
| Spring Data JPA | ORM（所有服务均使用） | — |
| JJWT | JWT 生成与验证 | 0.11.5 |
| MySQL | 主数据库，库名 `lingchat` | 8.0.33 |
| Redis | 缓存/Token 存储/在线状态 | — |
| Lombok | 简化 POJO 代码 | — |
| Kafka | 消息队列（规划中，未实现） | — |
| Netty | WebSocket 长连接（规划中） | — |

---

## 核心设计决策

### 1. 认证流程

```
Client → Gateway(8080) → [AuthFilter]
  → 放行 /api/auth/** (无需 Token)
  → 验证其他请求的 Authorization: Bearer <JWT>
  → 提取 userId，注入 X-User-Id Header 到下游请求
  → 下游服务通过 @RequestHeader("X-User-Id") 获取当前用户 ID
```

下游服务**不需要**再做 Token 验证，直接信任 `X-User-Id` Header。

### 2. 统一响应格式

所有接口使用 `lingchat-common` 中的 `Result<T>` 封装：

```java
Result.success(data)   // code=200
Result.fail("message") // code=500
```

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 3. 雪花算法 ID

`lingchat-common` 中的 `IdGenerator` 提供雪花算法分布式 ID，基于 MAC 地址计算机器 ID。消息 ID 等分布式 ID 应使用此工具类。

### 4. JPA 建表策略

所有服务的 `application.yaml` 均配置 `ddl-auto: update`，JPA 自动建表/更新表结构。**不使用手动 SQL 脚本**。

### 5. 服务间通信

当前为**直连（静态 URL）**，Nacos 服务注册代码已注释预留。未来接入 Nacos 时取消注释即可。

---

## lingchat-common 公共模块内容

这是最重要的基础模块，新服务开发前必须了解：

```
lingchat-common/src/main/java/org/lingchat/lingchatcommon/
├── model/
│   ├── Result<T>       ← 统一响应封装（必须使用）
│   ├── PageResult<T>   ← 分页结果封装
│   └── BaseEntity      ← JPA 实体公共父类（含 createTime/updateTime）
├── enums/
│   ├── ErrorCode       ← 错误码枚举（2xx参数/3xx认证/4xx用户/5xx消息/6xx系统）
│   ├── MessageType     ← 消息类型（TEXT/IMAGE/FILE/VOICE/VIDEO/SYSTEM）
│   └── UserStatusEnum  ← 用户状态（ONLINE/OFFLINE/AWAY/DO_NOT_DISTURB/INVISIBLE）
├── exception/
│   └── BusinessException ← 业务异常（支持 ErrorCode 构造）
├── constant/
│   └── RedisKeyConstant  ← Redis Key 前缀常量
└── utils/
    ├── IdGenerator     ← 雪花算法 ID 生成器
    └── StringUtils     ← 字符串工具类
```

---

## 数据库（MySQL）

- **数据库名**：`lingchat`
- **连接**：`localhost:3306`，用户名 `root`，密码 `123456`
- **当前已有表**：`user`、`user_profile`（x2，有重复问题）、`friendship`、`friend_request`

**已知问题**：`user_profile` 在 `auth-service` 和 `user-service` 中各定义了一份，字段略有差异（user-service 版本多 `signature` 字段）。这是一个待解决的技术债。

---

## 已知 Bug 与技术债

开发新功能时请注意这些问题，避免踩坑：

| # | 位置 | 问题 | 建议修复方案 |
|---|------|------|------------|
| 1 | `auth-service/GlobalExceptionHandler` | 缺少 `@RestControllerAdvice` 注解，异常处理不生效 | 添加注解 |
| 2 | `user-service/UserProfile` 实体 | 无 `status` 字段，但 `updateOnlineStatus` 方法试图更新状态 | 添加 `status` 字段或改为 Redis 存储 |
| 3 | `FriendServiceImpl.sendFriendRequest` | 使用 `findAll()` 全表查询后流式过滤 | 改为 `findBySenderIdAndReceiverIdAndStatus()` |
| 4 | `user_profile` 表定义重复 | auth-service 和 user-service 各自维护 | 注册时由 auth-service 调用 user-service API 创建 profile |
| 5 | `TestRedis.java`（gateway 模块） | 临时测试类混入生产代码 | 删除或移到 test 目录 |
| 6 | 密钥/密码硬编码 | JWT 密钥、DB 密码写在 yaml 中 | 生产环境改用环境变量 |
| 7 | 大量 `System.out.println` | 应使用 Slf4j logger | 全局替换 |

---

## 开发规范

### 代码风格

- 使用 **Lombok** 减少样板代码（`@Data`、`@Builder`、`@Slf4j` 等）
- Controller 层只做参数接收和响应封装，业务逻辑放 Service
- Service 接口 + Impl 实现类分离
- Repository 层使用 Spring Data JPA，命名规范查询方法优先
- 异常统一用 `BusinessException` 或 `RuntimeException`，由 `GlobalExceptionHandler` 处理

### 新服务创建步骤

1. 在父 `pom.xml` 的 `<modules>` 中添加新模块
2. 创建模块目录和 `pom.xml`（参考 user-service 的 pom 结构）
3. 创建 `application.yaml`（配置端口、数据库、Redis）
4. 创建主启动类（`@SpringBootApplication`）
5. 按 `entity → repository → service → controller` 顺序开发
6. 在 **gateway** 的路由配置中添加新服务的路由规则

### 包结构规范（以新服务为例）

```
org.lingchat.{servicename}/
├── controller/    ← REST 接口层
├── service/       ← 业务逻辑接口
│   └── impl/      ← 业务逻辑实现
├── entity/        ← JPA 实体类
├── repository/    ← Spring Data JPA 接口
├── dto/
│   ├── request/   ← 入参 DTO
│   └── response/  ← 出参 DTO
├── config/        ← 配置类
└── exception/     ← 异常处理（GlobalExceptionHandler）
```

### API 设计规范

- 路径前缀：`/api/{service-name}/...`
- 使用标准 HTTP 方法（GET/POST/PUT/DELETE）
- 当前用户 ID 从 `@RequestHeader("X-User-Id")` 获取，不要从 Token 里解析
- 所有接口返回 `Result<T>` 封装

---

## 下一步开发重点

当前处于 **用户服务已完成，消息服务待开始** 阶段。按计划，下一个里程碑是：

### M6：修复技术债（建议先做）
1. 修复 `auth-service` 的 `GlobalExceptionHandler` 缺失注解问题
2. 修复 `user-profile` 表重复定义问题
3. 修复 `FriendServiceImpl` 全表查询性能问题
4. 修复 `updateOnlineStatus` 状态未持久化问题

### M7：消息服务（核心，最复杂）
需要创建 `lingchat-message-service` 模块，实现：
- Netty WebSocket 服务器（处理客户端长连接）
- Kafka 生产者（接收消息后发布到 Kafka topic）
- Kafka 消费者（消费消息后持久化到 MySQL）
- 离线消息存储与拉取接口
- 消息 ACK 确认机制

**关键：消息 ID 必须使用 `IdGenerator`（雪花算法），保证有序和唯一。**

### M9：前端（可与 M7 并行）
- 使用 `npm create vite@latest lingchat-web -- --template react-ts` 创建
- 放在项目根目录下的 `lingchat-web/` 目录

---

## 常用命令

```bash
# 编译整个多模块项目
mvn clean compile

# 跳过测试打包
mvn clean package -DskipTests

# 单独启动某个服务（在对应模块目录下）
mvn spring-boot:run

# 查看 MySQL 当前表结构（PowerShell / cmd）
mysql -u root -p123456 lingchat -e "show tables;"
```

**启动顺序**（有依赖关系）：

```
1. MySQL 和 Redis 先启动
2. lingchat-auth-service（8081）
3. lingchat-user-service（8082）
4. lingchat-gateway（8080）—— 最后启动网关
```

---

## 重要文件索引

| 文件路径 | 说明 |
|---------|------|
| `pom.xml` | 父工程 POM，管理所有模块和依赖版本 |
| `lingchat-common/src/main/java/.../model/Result.java` | 统一响应类（重要） |
| `lingchat-common/src/main/java/.../enums/ErrorCode.java` | 错误码枚举 |
| `lingchat-common/src/main/java/.../utils/IdGenerator.java` | 雪花算法 ID 生成器 |
| `lingchat-common/src/main/java/.../constant/RedisKeyConstant.java` | Redis Key 常量 |
| `lingchat-gateway/src/main/java/.../filter/AuthFilter.java` | JWT 验证网关过滤器 |
| `lingchat-gateway/src/main/resources/application.yaml` | 网关路由配置（新增服务需修改此文件） |
| `lingchat-auth-service/src/main/resources/application.yaml` | 认证服务配置（含 JWT 密钥） |
| `PRD.md` | 产品需求文档（功能规划、接口列表、里程碑） |
| `guidance/` | AI 对话历史（仅供参考，不是代码） |
