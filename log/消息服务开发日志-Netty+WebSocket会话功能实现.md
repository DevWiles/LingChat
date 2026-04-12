# 消息服务开发日志 - Netty + WebSocket 会话功能实现

> 开发日期：2026-04-13
> 开发者：Claude Code
> 功能：基于 Netty + WebSocket 实现私聊会话功能

---

## 一、需求理解

### 1.1 背景
项目已有用户认证和好友关系功能，下一步需要实现即时通讯（IM）的核心功能——消息收发。

### 1.2 目标
实现一个消息服务模块 `lingchat-message-service`，让用户之间可以实时发送和接收消息。

### 1.3 技术选型
- **WebSocket**：一种在单个 TCP 连接上进行全双工通信的协议。相比 HTTP 每次请求都要建立连接，WebSocket 只需握手一次，之后可以持续双向通信，非常适合即时通讯场景。
- **Netty**：一个高性能的 Java 网络框架，比原生的 Java NIO 更易用、更稳定。很多知名开源项目（如 Dubbo、RocketMQ）都使用它。

---

## 二、架构设计思考

### 2.1 整体流程
```
用户A 发消息 → 网关转发 → 消息服务处理 → 存储数据库 → 推送给用户B
```

### 2.2 关键问题与解决方案

**问题1：WebSocket 如何认证用户身份？**

HTTP 请求可以通过 Header 携带 Token，但 WebSocket 握手时不方便设置 Header。

**解决方案**：在 WebSocket 连接的 URL 中传递 Token：
```
ws://localhost:8080/ws?token=eyJhbGciOiJIUzI1NiJ9...
```

网关在握手阶段从 URL 参数提取 Token，验证后放行。

---

**问题2：如何知道用户是否在线？**

如果用户 A 给用户 B 发消息，我们需要知道 B 是否在线，才能决定是直接推送还是存储为离线消息。

**解决方案**：用 Redis 存储用户连接状态：
- 用户上线时：存储 `userId -> channelId` 映射
- 用户下线时：删除映射
- 发消息时：查 Redis 判断接收者是否在线

---

**问题3：如何保证消息 ID 唯一且有序？**

分布式系统中，多个服务可能同时生成消息 ID，普通的自增 ID 会冲突。

**解决方案**：使用雪花算法（Snowflake），生成 64 位的长整型 ID：
- 时间戳（41位）+ 机器ID（10位）+ 序列号（12位）
- 保证全局唯一、趋势递增

---

## 三、模块结构设计

```
lingchat-message-service/
├── config/          # 配置类
├── server/          # Netty 服务器相关
├── handler/         # WebSocket 消息处理器
├── entity/          # 数据库实体
├── repository/      # 数据访问层
├── service/         # 业务逻辑层
├── dto/             # 数据传输对象
├── controller/      # REST 接口
└── exception/       # 异常处理
```

---

## 四、核心实现步骤

### Step 1：创建模块骨架

1. 在父 `pom.xml` 中添加新模块
2. 创建 `lingchat-message-service` 目录
3. 编写 `pom.xml`，添加依赖：
   - `lingchat-common`：公共模块
   - `netty-all`：Netty 框架
   - `jjwt`：JWT 解析
   - `spring-boot-starter-data-jpa`：数据库操作
   - `spring-boot-starter-data-redis`：Redis 操作

### Step 2：设计消息数据模型

**Message 实体**：
| 字段 | 类型 | 说明 |
|------|------|------|
| messageId | Long | 消息ID（雪花算法） |
| senderId | Long | 发送者ID |
| receiverId | Long | 接收者ID |
| type | Integer | 消息类型（1文本/2图片/3文件...） |
| content | String | 消息内容 |
| status | Integer | 状态（0发送中/1已送达/2已读） |
| createTime | DateTime | 创建时间 |

**OfflineMessage 实体**：
| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 接收者ID |
| messageId | Long | 消息ID |

### Step 3：实现会话管理（SessionService）

```java
// 用户上线
void registerSession(Long userId, String channelId);

// 用户下线
void removeSession(Long userId);

// 查询用户是否在线
boolean isOnline(Long userId);
```

Redis 存储结构：
- `lingchat:session:user:{userId}` = `channelId`
- `lingchat:session:id:{channelId}` = `userId`

### Step 4：配置网关 WebSocket 路由

修改 `lingchat-gateway/application.yaml`：
```yaml
routes:
  - id: message-service-ws
    uri: ws://localhost:8083
    predicates:
      - Path=/ws
```

修改 `AuthFilter.java`：
- 检测 WebSocket 握手请求（Header 中 `Upgrade: websocket`）
- 从 URL 参数提取 Token 并验证

### Step 5：实现 Netty WebSocket 服务器

**核心组件**：

1. **NettyWebSocketServer**：启动 Netty 服务
   - BossGroup：处理连接
   - WorkerGroup：处理 I/O

2. **WebSocketChannelInitializer**：配置 Channel 管道
   - HTTP 编解码器
   - 消息聚合器
   - 心跳检测器（120秒无心跳断开）
   - WebSocket 协议处理器

3. **WebSocketHandler**：处理消息
   - 握手时验证 Token，绑定用户
   - 接收消息时解析并处理
   - 断开时清理会话

4. **ChannelManager**：管理 Channel 与用户的映射
   - 本地内存缓存（ConcurrentHashMap）

### Step 6：设计消息协议

**客户端 → 服务端**：
```json
// 发送消息
{"type":"chat","to":123,"msgType":1,"content":"Hello"}

// 心跳
{"type":"heartbeat"}

// 确认收到
{"type":"ack","msgId":456}
```

**服务端 → 客户端**：
```json
// 推送消息
{"type":"chat","msgId":789,"from":123,"msgType":1,"content":"Hello","time":"..."}

// 确认发送成功
{"type":"ack","msgId":789,"status":1}

// 心跳响应
{"type":"pong"}
```

### Step 7：实现消息业务逻辑（MessageService）

```java
// 发送消息流程
1. 生成消息ID（雪花算法）
2. 保存消息到数据库
3. 发送 ACK 确认给发送者
4. 判断接收者是否在线：
   - 在线：直接推送
   - 离线：存储到 offline_message 表
```

### Step 8：离线消息处理

用户上线后：
1. 查询 `offline_message` 表
2. 批量推送离线消息
3. 客户端 ACK 后删除离线记录

---

## 五、遇到的编译错误及解决

### 错误 1：找不到 IdleStateEvent 类
**原因**：缺少 import 语句
**解决**：添加 `import io.netty.handler.timeout.IdleStateEvent;`

### 错误 2：Integer 无法转换为 MessageType
**原因**：实体类的 type 字段定义为 Integer，但错误地尝试传入枚举类型
**解决**：直接使用 Integer 类型存储，移除枚举转换代码

### 错误 3：Channel 无法转换为 ChannelHandlerContext
**原因**：`sendMessage` 方法重载时参数类型混淆
**解决**：添加一个参数为 `Channel` 类型的重载方法

---

## 六、测试方法

1. **启动服务**：
   - MySQL、Redis
   - auth-service (8081)
   - user-service (8082)
   - message-service (8083)
   - gateway (8080)

2. **获取 Token**：
   ```
   POST http://localhost:8080/api/auth/login
   {"username":"user1","password":"123456"}
   ```

3. **WebSocket 连接**：
   使用 Postman 或 wscat 连接：
   ```
   ws://localhost:8080/ws?token=<your_token>
   ```

4. **发送消息**：
   ```json
   {"type":"chat","to":2,"msgType":1,"content":"你好"}
   ```

---

## 七、总结

本次开发实现了一个完整的即时通讯消息服务，核心要点：

1. **WebSocket 认证**：通过 URL 参数传递 Token，在握手阶段验证
2. **会话管理**：使用 Redis 存储用户在线状态
3. **消息 ID**：使用雪花算法保证分布式唯一
4. **离线消息**：存储到数据库，用户上线后推送
5. **心跳机制**：120 秒无心跳自动断开连接

这个实现虽然简单，但涵盖了 IM 系统的核心功能。后续可以扩展：
- Kafka 消息队列（削峰填谷）
- 消息已读回执
- 群聊消息
- 图片/文件消息

---

## 八、文件清单

| 文件路径 | 作用 |
|---------|------|
| `lingchat-message-service/pom.xml` | 模块依赖配置 |
| `lingchat-message-service/src/main/resources/application.yaml` | 服务配置 |
| `entity/Message.java` | 消息实体类 |
| `entity/OfflineMessage.java` | 离线消息实体 |
| `service/SessionService.java` | 会话管理接口 |
| `service/impl/SessionServiceImpl.java` | Redis 会话管理 |
| `service/MessageService.java` | 消息业务接口 |
| `service/impl/MessageServiceImpl.java` | 消息业务实现 |
| `server/NettyWebSocketServer.java` | Netty 服务启动 |
| `server/ChannelManager.java` | Channel 管理器 |
| `server/WebSocketChannelInitializer.java` | Channel 初始化 |
| `handler/WebSocketHandler.java` | WebSocket 消息处理 |
| `controller/MessageController.java` | REST API |
| `lingchat-gateway/.../AuthFilter.java` | WebSocket 认证支持 |
| `lingchat-gateway/.../application.yaml` | WebSocket 路由配置 |
