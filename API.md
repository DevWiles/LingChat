# LingChat API 文档

本文档记录 LingChat 所有微服务的 REST API 和 WebSocket 接口。

---

## 通用说明

### 统一响应格式

所有 REST API 接口均使用 `Result<T>` 统一封装响应：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200 表示成功，其他为错误码 |
| message | String | 响应消息 |
| data | T | 响应数据，成功时返回业务数据，失败时可能为 null |

### 认证方式

- **REST API**: 除认证接口外，所有请求需在 Header 中携带 `Authorization: Bearer <JWT Token>`
- **WebSocket**: 连接时需在 URL 参数中携带 token，如 `ws://host:port/ws?token=<JWT Token>`

### 用户身份传递

网关（Gateway）验证 JWT 后，会提取 `userId` 并注入到请求头 `X-User-Id` 中，下游服务通过此 Header 获取当前登录用户 ID。

---

## 1. 认证服务 (auth-service)

**服务端口**: 8081
**前缀路径**: `/api/auth`

### 1.1 用户注册

**POST** `/api/auth/register`

无需认证。

**请求体**:
```json
{
  "username": "string",    // 用户名，必填
  "password": "string",    // 密码，必填
  "nickname": "string",    // 昵称，可选
  "avatar": "string"       // 头像 URL，可选
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123456789,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar.png",
    "status": 1,
    "createTime": "2026-04-16T10:30:00",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### 1.2 用户登录

**POST** `/api/auth/login`

无需认证。

**请求体**:
```json
{
  "username": "string",    // 用户名，必填
  "password": "string"     // 密码，必填
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123456789,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar.png",
    "status": 1,
    "createTime": "2026-04-16T10:30:00",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### 1.3 根据用户名查询用户

**GET** `/api/auth/user/{username}`

无需认证。

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| username | String | 用户名 |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123456789,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar.png",
    "status": 1,
    "createTime": "2026-04-16T10:30:00",
    "token": null
  }
}
```

---

## 2. 用户服务 (user-service)

**服务端口**: 8082
**前缀路径**: `/api/user`

### 2.1 初始化用户档案（内部接口）

**POST** `/api/user/profile/init`

由 auth-service 在用户注册成功后内部调用，无需认证。

**请求体**:
```json
{
  "userId": 123456789,       // 用户ID，必填
  "username": "testuser",    // 用户名，必填
  "nickname": "测试用户",     // 昵称，必填
  "avatar": "https://..."    // 头像 URL，可选
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 2.2 获取用户档案

**GET** `/api/user/profile/{userId}`

需认证。

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 123456789,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar.png",
    "signature": "这是个性签名",
    "createTime": "2026-04-16T10:30:00"
  }
}
```

---

### 2.3 更新用户档案

**PUT** `/api/user/profile`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**请求体**:
```json
{
  "nickname": "string",     // 昵称，可选
  "avatar": "string",       // 头像 URL，可选
  "signature": "string"     // 个性签名，可选
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 123456789,
    "username": "testuser",
    "nickname": "新昵称",
    "avatar": "https://example.com/new-avatar.png",
    "signature": "新的个性签名",
    "createTime": "2026-04-16T10:30:00"
  }
}
```

---

### 2.4 搜索用户

**GET** `/api/user/search`

需认证。

**查询参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| username | String | 用户名 |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 123456789,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar.png",
    "signature": "这是个性签名",
    "createTime": "2026-04-16T10:30:00"
  }
}
```

---

## 3. 好友服务 (user-service)

**服务端口**: 8082
**前缀路径**: `/api/friend`

### 3.1 发送好友申请

**POST** `/api/friend/request`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**请求体**:
```json
{
  "friendId": 123456789,    // 目标用户ID，必填
  "message": "string"       // 申请消息，可选
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": "好友申请已发送"
}
```

---

### 3.2 处理好友申请

**POST** `/api/friend/request/handle`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**查询参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| requestId | Long | 好友申请ID |
| agree | Boolean | 是否同意（true/false） |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": "已同意好友申请"   // 或 "已拒绝好友申请"
}
```

---

### 3.3 获取好友列表

**GET** `/api/friend/list`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "userId": 123456789,
      "username": "friend1",
      "nickname": "好友1",
      "avatar": "https://example.com/avatar1.png",
      "signature": "个性签名1"
    },
    {
      "userId": 987654321,
      "username": "friend2",
      "nickname": "好友2",
      "avatar": "https://example.com/avatar2.png",
      "signature": "个性签名2"
    }
  ]
}
```

---

### 3.4 删除好友

**DELETE** `/api/friend/{friendId}`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| friendId | Long | 好友用户ID |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": "已删除好友"
}
```

---

### 3.5 设置好友备注

**PUT** `/api/friend/remark`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**请求体**:
```json
{
  "friendId": 123456789,    // 好友用户ID，必填
  "remark": "string"        // 备注名，必填
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": "备注已更新"
}
```

---

### 3.6 加入黑名单

**PUT** `/api/friend/blacklist/{friendId}`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| friendId | Long | 好友用户ID |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": "已加入黑名单"
}
```

---

### 3.7 移出黑名单

**DELETE** `/api/friend/blacklist/{friendId}`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| friendId | Long | 好友用户ID |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": "已移出黑名单"
}
```

---

### 3.8 获取待处理好友申请

**GET** `/api/friend/requests/pending`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 111222333,
      "senderId": 123456789,
      "senderUsername": "sender1",
      "senderNickname": "发送者1",
      "message": "你好，加个好友吧",
      "createTime": "2026-04-16T10:30:00"
    }
  ]
}
```

---

## 4. 消息服务 (message-service)

**服务端口**: 8084 (REST API), 8083 (WebSocket)
**前缀路径**: `/api/message`

### 4.1 REST API

#### 4.1.1 获取聊天记录

**GET** `/api/message/history/{friendId}`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| friendId | Long | 好友用户ID |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "messageId": 1234567890123456789,
      "senderId": 123456789,
      "receiverId": 987654321,
      "type": 1,
      "content": "你好",
      "status": 2,
      "createTime": "2026-04-16T10:30:00"
    }
  ]
}
```

**消息类型 (type)**:
| 值 | 说明 |
|----|------|
| 1 | 文本消息 |
| 2 | 图片消息 |
| 3 | 文件消息 |
| 4 | 语音消息 |
| 5 | 视频消息 |

**消息状态 (status)**:
| 值 | 说明 |
|----|------|
| 0 | 发送中 |
| 1 | 已送达 |
| 2 | 已读 |
| 3 | 已撤回 |

---

#### 4.1.2 获取离线消息

**GET** `/api/message/offline`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "messageId": 1234567890123456789,
      "senderId": 123456789,
      "receiverId": 987654321,
      "type": 1,
      "content": "你好",
      "status": 1,
      "createTime": "2026-04-16T10:30:00"
    }
  ]
}
```

---

#### 4.1.3 标记消息已读

**PUT** `/api/message/read/{messageId}`

需认证。

**请求头**:
| Header | 类型 | 说明 |
|--------|------|------|
| X-User-Id | Long | 当前用户ID（由网关注入） |

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| messageId | Long | 消息ID |

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 4.2 WebSocket 接口

**连接地址**: `ws://localhost:8083/ws?token=<JWT Token>`

WebSocket 使用 Netty 实现，连接时需在 URL 参数中携带 JWT Token 进行认证。

#### 4.2.1 连接流程

1. 客户端发起 WebSocket 连接请求：`ws://localhost:8083/ws?token=<JWT>`
2. 服务端验证 Token，提取 userId
3. 验证成功后建立连接，绑定用户与 Channel
4. 服务端自动推送离线消息给客户端

#### 4.2.2 客户端消息格式

所有消息均为 JSON 格式，包含 `type` 字段区分消息类型。

##### 发送聊天消息

```json
{
  "type": "chat",
  "to": 987654321,       // 接收者用户ID
  "msgType": 1,          // 消息类型：1=文本, 2=图片, 3=文件, 4=语音, 5=视频
  "content": "你好"       // 消息内容
}
```

##### 心跳请求

```json
{
  "type": "heartbeat"
}
```

建议客户端每 30 秒发送一次心跳，服务端会检测连接空闲超时并断开。

##### 消息确认 (ACK)

```json
{
  "type": "ack",
  "msgId": 1234567890123456789    // 已读消息ID
}
```

---

#### 4.2.3 服务端消息格式

##### 聊天消息推送

```json
{
  "type": "chat",
  "msgId": 1234567890123456789,
  "from": 123456789,
  "to": 987654321,
  "msgType": 1,
  "content": "你好",
  "time": "2026-04-16 10:30:00"
}
```

##### 心跳响应

```json
{
  "type": "pong"
}
```

##### 消息确认响应

```json
{
  "type": "ack",
  "msgId": 1234567890123456789,
  "status": 2    // 消息状态
}
```

---

#### 4.2.4 消息类型说明

| msgType | 说明 |
|---------|------|
| 1 | 文本消息 |
| 2 | 图片消息 |
| 3 | 文件消息 |
| 4 | 语音消息 |
| 5 | 视频消息 |

#### 4.2.5 消息状态说明

| status | 说明 |
|--------|------|
| 0 | 发送中 |
| 1 | 已送达 |
| 2 | 已读 |
| 3 | 已撤回 |

---

## 5. 网关服务 (gateway)

**服务端口**: 8080

网关负责请求路由和 JWT 验证。

### 路由规则

| 路径前缀 | 目标服务 | 端口 |
|----------|----------|------|
| `/api/auth/**` | auth-service | 8081 |
| `/api/user/**` | user-service | 8082 |
| `/api/friend/**` | user-service | 8082 |
| `/api/message/**` | message-service | 8084 |

### 认证过滤器

- 放行 `/api/auth/**` 路径（无需认证）
- 其他路径需要 `Authorization: Bearer <JWT>` 请求头
- 验证成功后注入 `X-User-Id` 请求头到下游服务

---

## 附录：错误码

错误码定义见 `lingchat-common` 模块中的 `ErrorCode` 枚举：

| 错误码范围 | 分类 |
|------------|------|
| 2xx | 参数错误 |
| 3xx | 认证/授权错误 |
| 4xx | 用户相关错误 |
| 5xx | 消息相关错误 |
| 6xx | 系统错误 |

---

*文档生成时间: 2026-04-16*
