# 灵雀轻聊（LingChat）产品需求文档

> **版本**：v0.3.0-dev  
> **更新日期**：2026-04-07  
> **状态**：开发中（后端约 60% 完成，前端未启动）

---

## 一、产品概述

### 1.1 产品定位

灵雀轻聊（LingChat）是一款基于 Spring Cloud 微服务架构的**轻量化即时通讯系统**，面向学习与求职展示场景，实现了类 Discord/微信 的核心聊天功能，技术实现上对标企业级 IM 系统架构。

### 1.2 目标用户

- 开发者本人（Java 后端求职作品集项目）
- 技术面试官（展示微服务、IM 系统设计能力）

### 1.3 核心价值

| 价值点 | 说明 |
|--------|------|
| 技术广度 | 覆盖 Spring Cloud、Kafka、Netty、Redis、WebSocket 等企业级技术栈 |
| 架构完整性 | 网关 → 认证 → 用户 → 消息 → 群聊 → 在线状态，链路完整 |
| 可演示性 | 前后端均实现，可本地运行完整 Demo |

---

## 二、功能需求

### 2.1 功能总览

```
优先级：P0（必须实现）/ P1（计划实现）/ P2（可选扩展）
```

| 功能模块 | 子功能 | 优先级 | 当前状态 |
|---------|--------|--------|---------|
| **用户认证** | 注册 | P0 | ✅ 已完成 |
| | 登录（JWT） | P0 | ✅ 已完成 |
| | 注销 / Token 失效 | P0 | ❌ 未实现 |
| | 修改密码 | P1 | ❌ 未实现 |
| **用户资料** | 查看资料 | P0 | ✅ 已完成 |
| | 修改昵称/头像/签名 | P0 | ✅ 已完成 |
| | 按用户名搜索 | P0 | ✅ 已完成 |
| | 在线状态设置 | P1 | ⚠️ 部分实现（存储未持久化） |
| **好友关系** | 发送好友申请 | P0 | ✅ 已完成 |
| | 同意/拒绝申请 | P0 | ✅ 已完成 |
| | 好友列表 | P0 | ✅ 已完成 |
| | 删除好友 | P0 | ✅ 已完成 |
| | 好友备注 | P1 | ✅ 已完成 |
| | 黑名单管理 | P1 | ✅ 已完成 |
| | 待处理申请查看 | P0 | ✅ 已完成 |
| **私聊消息** | 发送文本消息 | P0 | ❌ 未实现 |
| | 发送图片/文件 | P1 | ❌ 未实现 |
| | 消息撤回 | P1 | ❌ 未实现 |
| | 离线消息 | P0 | ❌ 未实现 |
| | 消息已读状态 | P1 | ❌ 未实现 |
| | 消息历史记录 | P0 | ❌ 未实现 |
| **实时通信** | WebSocket 长连接 | P0 | ❌ 未实现 |
| | 心跳检测 | P0 | ❌ 未实现 |
| | 在线状态广播 | P1 | ❌ 未实现 |
| **群聊** | 创建群组 | P1 | ❌ 未实现 |
| | 群成员管理 | P1 | ❌ 未实现 |
| | 群聊消息 | P1 | ❌ 未实现 |
| **前端界面** | 登录/注册页 | P0 | ❌ 未实现 |
| | 联系人列表 | P0 | ❌ 未实现 |
| | 聊天对话框 | P0 | ❌ 未实现 |
| | 好友管理界面 | P0 | ❌ 未实现 |

### 2.2 用户认证模块（已完成）

**注册接口**

- 入参：`username`（唯一）、`password`（BCrypt 加密存储）、`nickname`、`avatar`
- 注册时同步创建 `user_profile` 记录
- 出参：统一 `Result<T>` 格式

**登录接口**

- 入参：`username`、`password`
- 验证通过后签发 JWT Token（有效期 24 小时）
- JWT Payload 包含 `userId`
- 出参：返回 JWT Token 字符串

**Token 验证**

- 网关层全局过滤器（`AuthFilter`）拦截所有非 `/api/auth/**` 请求
- 验证 `Authorization: Bearer <token>` Header
- 验证通过后将 `userId` 注入下游 Header `X-User-Id`
- 下游服务无需再做 Token 验证

### 2.3 消息服务（待实现）

这是项目的**核心 IM 功能**，当前为最高优先级待开发模块。

**消息发送流程**（规划）：

```
Client --WebSocket--> Gateway/Netty
  --> message-service (写入 Kafka)
  --> Kafka Consumer (持久化 MySQL)
  --> 推送至接收方 WebSocket 连接
  --> 若接收方离线：存入离线消息队列
```

**消息数据模型**（规划）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `messageId` | BIGINT | 雪花算法生成 |
| `senderId` | BIGINT | 发送者 |
| `receiverId` | BIGINT | 接收者（私聊）或 groupId（群聊） |
| `type` | ENUM | TEXT/IMAGE/FILE/VOICE/VIDEO/SYSTEM |
| `content` | TEXT | 消息内容（文本）或 URL（媒体） |
| `status` | INT | 0=发送中 1=已送达 2=已读 3=撤回 |
| `createTime` | DATETIME | 发送时间 |
| `isGroupMsg` | BOOLEAN | 是否群消息 |

**IM 系统核心难点**（需重点实现）：

- 消息 ACK 确认机制
- 离线消息拉取（用户上线后拉取离线期间的消息）
- 消息序列号（保证消息有序性）
- 消息去重（网络重传场景下的幂等性）

### 2.4 在线状态服务（待实现）

- 用 Redis 存储在线用户集合（`user:online:{userId}`）
- WebSocket 断连时自动标记离线
- 心跳检测：客户端每 30 秒发送一次 Ping

---

## 三、系统架构

### 3.1 整体架构

```
┌─────────────────────────────────────────────┐
│                  Client                      │
│            (React + TypeScript)              │
└──────────────────┬──────────────────────────┘
                   │ HTTP / WebSocket
┌──────────────────▼──────────────────────────┐
│           API Gateway (8080)                 │
│   Spring Cloud Gateway (WebFlux)             │
│   ┌─────────────────────────────────────┐   │
│   │  JWT AuthFilter  │  LoggingFilter   │   │
│   │  IP 限流         │  CORS 配置       │   │
│   └─────────────────────────────────────┘   │
└──┬───────────┬───────────┬──────────────────┘
   │           │           │
   ▼           ▼           ▼
auth(8081) user(8082) message(8083)  group(8084)  presence(8085)
   │           │           │
   └─────┬─────┘           │
         │                 │
      MySQL             Kafka ←→ MySQL
         │
       Redis（缓存 / 在线状态 / Token 黑名单）
```

### 3.2 微服务模块规划

| 模块 | 端口 | 状态 | 职责 |
|------|------|------|------|
| `lingchat-common` | — | ✅ 完成 | 公共代码（枚举/工具/统一响应/异常） |
| `lingchat-gateway` | 8080 | ✅ 完成 | API 网关、JWT 验证、路由转发 |
| `lingchat-auth-service` | 8081 | ✅ 完成 | 注册、登录、JWT 签发 |
| `lingchat-user-service` | 8082 | ✅ 完成 | 用户资料、好友关系 |
| `lingchat-message-service` | 8083 | ❌ 待开发 | 消息发送、存储、推送、离线消息 |
| `lingchat-group-service` | 8084 | ❌ 待开发 | 群组管理、群消息 |
| `lingchat-presence-service` | 8085 | ❌ 待开发 | 在线状态、心跳检测 |
| 前端（lingchat-web） | 3000 | ❌ 待开发 | React + TypeScript + Vite |

### 3.3 数据库设计（已实现部分）

**当前已有表**（MySQL `lingchat` 库）：

```sql
-- 认证服务（auth-service）
user             -- 账号认证信息
user_profile     -- 用户展示信息（auth-service 版）

-- 用户服务（user-service）
user_profile     -- 用户展示信息（含 signature 字段）
friendship       -- 好友关系（双向记录）
friend_request   -- 好友申请记录
```

> ⚠️ **注意**：`user_profile` 在 auth-service 和 user-service 中各有一份定义，需要后续统一（建议 auth-service 注册时写入 user-service 的 profile 表，auth-service 自身不维护 profile）。

**规划中的表**：

```sql
-- message-service
message          -- 消息记录
offline_message  -- 离线消息队列

-- group-service
chat_group       -- 群组信息
group_member     -- 群成员
group_message    -- 群消息
```

---

## 四、技术栈

### 4.1 后端

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.2.5 |
| 微服务 | Spring Cloud | 2023.0.1 |
| 网关 | Spring Cloud Gateway (WebFlux) | 内含 |
| ORM | Spring Data JPA / Hibernate | 内含 |
| 认证 | Spring Security + JJWT | 0.11.5 |
| 缓存 | Redis (Spring Data Redis) | — |
| 数据库 | MySQL | 8.0.33 |
| 消息队列 | Kafka（规划中） | — |
| 长连接 | Netty WebSocket（规划中） | — |
| 工具 | Lombok、Maven | — |

### 4.2 前端（规划中）

| 分类 | 技术 |
|------|------|
| 框架 | React 18 |
| 语言 | TypeScript |
| 构建工具 | Vite |
| 状态管理 | Zustand / Redux Toolkit |
| UI 组件库 | Ant Design |
| HTTP 客户端 | Axios |
| WebSocket | 原生 WebSocket API |

### 4.3 基础设施（规划中）

- Docker + Docker Compose（服务容器化）
- Nacos（服务注册与发现，代码中已预留注释）
- JaCoCo（代码覆盖率，common 模块已配置）

---

## 五、API 接口规范

### 5.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

错误码规范（见 `ErrorCode` 枚举）：

| 范围 | 说明 |
|------|------|
| 2xx | 参数错误 |
| 3xx | 认证授权错误 |
| 4xx | 用户业务错误 |
| 5xx | 消息业务错误 |
| 6xx | 系统错误 |

### 5.2 已实现接口列表

**认证服务（经网关：`http://localhost:8080`）**

| Method | Path | 认证 | 描述 |
|--------|------|------|------|
| POST | `/api/auth/register` | 否 | 用户注册 |
| POST | `/api/auth/login` | 否 | 用户登录，返回 JWT |
| GET | `/api/auth/user/{username}` | 是 | 按用户名查用户 |

**用户服务（经网关，需 JWT）**

| Method | Path | 描述 |
|--------|------|------|
| GET | `/api/user/profile/{userId}` | 获取用户资料 |
| PUT | `/api/user/profile` | 更新用户资料 |
| GET | `/api/user/search?username=` | 按用户名搜索 |
| POST | `/api/friend/request` | 发送好友申请 |
| POST | `/api/friend/request/handle` | 处理好友申请 |
| GET | `/api/friend/list` | 获取好友列表 |
| DELETE | `/api/friend/{friendId}` | 删除好友 |
| PUT | `/api/friend/remark` | 设置好友备注 |
| PUT | `/api/friend/blacklist/{friendId}` | 加入黑名单 |
| DELETE | `/api/friend/blacklist/{friendId}` | 移出黑名单 |
| GET | `/api/friend/requests/pending` | 获取待处理申请 |

---

## 六、已知问题与技术债

| 问题 | 严重程度 | 影响 |
|------|---------|------|
| `user_profile` 表在两个服务中重复定义 | 高 | 数据不一致风险 |
| auth-service `GlobalExceptionHandler` 缺少 `@RestControllerAdvice` | 高 | 异常处理不生效 |
| `FriendServiceImpl.sendFriendRequest` 使用 `findAll()` 全表查询 | 中 | 数据量大时性能差 |
| `updateOnlineStatus` 状态未实际持久化（`UserProfile` 无 `status` 字段） | 中 | 在线状态功能无效 |
| JWT 密钥和数据库密码硬编码在 `application.yaml` | 中 | 安全隐患 |
| `TestRedis.java` 临时测试类混入生产代码 | 低 | 代码质量 |
| 大量 `System.out.println` 调试输出（应改为 Slf4j） | 低 | 日志不规范 |
| 网关测试代码编译失败 | 低 | 无法运行测试 |

---

## 七、开发里程碑

### 已完成
- [x] **M1** - 项目骨架搭建（Maven 多模块）
- [x] **M2** - 公共模块（common）完成
- [x] **M3** - 认证服务完成（注册/登录/JWT）
- [x] **M4** - 网关服务完成（路由/鉴权/限流）
- [x] **M5** - 用户服务完成（资料/好友关系）

### 待开发（按优先级）

- [ ] **M6** - 修复现有技术债（user_profile 统一、异常处理修复）
- [ ] **M7** - 消息服务开发（`lingchat-message-service`）
  - WebSocket 长连接接入（Netty）
  - Kafka 消息队列集成
  - 消息持久化存储
  - 离线消息处理
- [ ] **M8** - 在线状态服务（`lingchat-presence-service`）
  - Redis 存储在线用户
  - 心跳检测机制
- [ ] **M9** - 前端开发（React + TypeScript）
  - 登录/注册页
  - 主聊天界面
  - 好友管理
- [ ] **M10** - 群聊服务（`lingchat-group-service`）
- [ ] **M11** - 容器化部署（Docker Compose）
