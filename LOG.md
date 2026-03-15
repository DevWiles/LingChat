我给你一套 **完整可用的 IM 微服务 Maven 项目骨架（LingChat 示例）**。
这是很多企业项目常见的 **Spring Boot + Maven 多模块 + 微服务结构**。

---

# 一、整体项目结构（推荐）

```text
LingChat
│
├─ pom.xml                         ← 父工程（不写代码）
│
├─ lingchat-common                 ← 公共模块
│
├─ lingchat-framework              ← 基础设施模块
│
├─ lingchat-gateway                ← 网关服务
│
├─ lingchat-auth-service           ← 登录认证服务
│
├─ lingchat-user-service           ← 用户服务
│
├─ lingchat-message-service        ← 消息服务
│
├─ lingchat-group-service          ← 群聊服务
│
├─ lingchat-presence-service       ← 在线状态服务
│
└─ lingchat-job                    ← 定时任务服务
```

每个 **service 模块都是一个独立的 SpringBoot 服务**。

---

# 二、父模块 pom.xml（核心）

父模块只负责 **管理依赖 + 管理模块**。

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">

    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>lingchat</artifactId>
    <version>1.0-SNAPSHOT</version>

    <packaging>pom</packaging>

    <!-- 所有子模块 -->
    <modules>
        <module>lingchat-common</module>
        <module>lingchat-framework</module>
        <module>lingchat-gateway</module>
        <module>lingchat-auth-service</module>
        <module>lingchat-user-service</module>
        <module>lingchat-message-service</module>
        <module>lingchat-group-service</module>
        <module>lingchat-presence-service</module>
        <module>lingchat-job</module>
    </modules>

    <!-- 统一版本 -->
    <properties>
        <java.version>17</java.version>
        <spring.boot.version>3.2.5</spring.boot.version>
        <mysql.version>8.3.0</mysql.version>
    </properties>

</project>
```

---

# 三、common 模块

作用：**所有服务共享代码**

```text
lingchat-common
│
├─ constant
├─ enums
├─ exception
├─ utils
└─ model
```

例如：

```
Result
ErrorCode
MessageType
UserStatus
```

---

# 四、framework 模块

作用：**基础设施封装**

```text
lingchat-framework
│
├─ config
├─ redis
├─ database
├─ mq
└─ security
```

通常封装：

* Redis
* MyBatis
* Kafka / RabbitMQ
* 日志
* 全局异常

---

# 五、gateway 模块

IM 系统入口。

```text
lingchat-gateway
│
├─ controller
├─ filter
├─ websocket
└─ config
```

功能：

```
HTTP入口
WebSocket入口
Token校验
请求路由
```

---

# 六、user-service

用户相关功能：

```text
lingchat-user-service
│
├─ controller
├─ service
├─ repository
└─ model
```

负责：

```
用户信息
好友关系
用户资料
黑名单
```

---

# 七、message-service（IM核心）

```text
lingchat-message-service
│
├─ controller
├─ service
├─ consumer
├─ producer
└─ repository
```

功能：

```
发送消息
离线消息
消息存储
消息推送
```

---

# 八、presence-service

在线状态服务：

```text
lingchat-presence-service
│
├─ service
├─ redis
└─ listener
```

负责：

```
用户上线
用户离线
在线列表
心跳检测
```

通常用 **Redis** 保存在线用户。

---

# 九、微服务端口示例

```text
lingchat-gateway           8080
lingchat-auth-service      8081
lingchat-user-service      8082
lingchat-message-service   8083
lingchat-group-service     8084
lingchat-presence-service  8085
```

每个模块：

```
一个 SpringBoot
一个端口
一个服务
```

---

# 十、IM 系统真实架构关系

```
Client
   │
   │
Gateway
   │
   ├── auth-service
   ├── user-service
   ├── message-service
   └── group-service
          │
          │
        MQ
          │
      presence-service
```

常见技术栈：

* Spring Boot
* Redis
* MySQL
* Netty
* Kafka / RabbitMQ

---

# 十一、开发顺序（非常关键）

很多人顺序写错。

推荐顺序：

```
1 gateway
2 auth-service
3 user-service
4 message-service
5 group-service
6 presence-service
```

因为：

```
登录 → 用户 → 消息
```

---

# 十二、IM系统真正难的部分

很多教程不会讲：

```
消息ACK确认
离线消息
消息重试
消息序列号
消息去重
```

这些才是 **IM系统核心难点**。

---

✅ 如果你愿意，我可以继续帮你画一张 **“LingChat 完整架构图（包含 Netty + MQ + Redis）”**。

那张图基本就是 **腾讯IM / Discord 类似的架构思路**，很多人看完会突然理解 **IM系统为什么要这么设计**。
