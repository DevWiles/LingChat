你这个问题其实很接近 **真实公司里的任务分配**：

> “只负责 gateway-service，从 0 到可上线，不能越权到别的服务。”

所以我给你一套 **企业级开发流程**，保证：

* ✅ 只做 **网关职责**
* ✅ 符合 **微服务规范**
* ✅ 代码 **可测试**
* ✅ 结构 **可扩展**
* ❌ 不写 auth / user / im 业务

下面是完整方案。

---

# 一、先明确：Gateway 在微服务里负责什么

网关的职责其实只有 5 个：

1️⃣ **统一入口**
所有请求先到 gateway

```
Client → Gateway → Service
```

例如

```
/api/user/login  → user-service
/api/chat/send   → chat-service
```

---

2️⃣ **路由转发**

根据路径转发：

```
/api/auth/** → auth-service
/api/user/** → user-service
/api/im/**   → im-service
```

---

3️⃣ **统一认证**

验证：

```
JWT
Token
API Key
```

例如

```
Authorization: Bearer xxx
```

---

4️⃣ **限流 / 防刷**

例如：

```
1秒 100 次
```

防止接口被刷爆。

---

5️⃣ **统一日志 / 监控**

例如：

```
请求路径
IP
耗时
```

---

# 二、网关不能做什么（非常重要）

你必须避免这些：

❌ 写数据库
❌ 写业务逻辑
❌ 写用户逻辑
❌ 写聊天逻辑
❌ 写订单逻辑

网关 **只做流量管理**。

---

# 三、技术选型（企业级）

推荐：

```
Spring Cloud Gateway
```

因为：

| 方案                   | 说明    |
| -------------------- | ----- |
| Nginx                | 纯反向代理 |
| Zuul                 | 已过时   |
| Spring Cloud Gateway | ⭐主流   |

---

# 四、项目结构设计

建议结构：

```
gateway-service
│
├── config
│   ├── GatewayConfig
│   ├── CorsConfig
│
├── filter
│   ├── AuthFilter
│   ├── LoggingFilter
│
├── route
│   ├── RouteLocatorConfig
│
├── util
│   ├── JwtUtil
│
├── exception
│   ├── GatewayExceptionHandler
│
└── GatewayApplication
```

职责：

| 目录        | 作用    |
| --------- | ----- |
| config    | 配置类   |
| filter    | 网关过滤器 |
| route     | 路由规则  |
| util      | 工具    |
| exception | 异常处理  |

---

# 五、第一步：创建项目

依赖：

```
spring-boot-starter-webflux
spring-cloud-starter-gateway
lombok
```

pom 示例：

```xml
<dependency>
 <groupId>org.springframework.cloud</groupId>
 <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

---

# 六、基础配置

application.yml

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/auth/**

        - id: user-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/user/**
```

含义：

```
/api/auth/** → auth-service
/api/user/** → user-service
```

---

# 七、认证过滤器（核心功能）

最重要的一部分：

```
filter/AuthFilter
```

示例：

```java
@Component
public class AuthFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String token = request.getHeaders().getFirst("Authorization");

        if(token == null || !token.startsWith("Bearer ")){

            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);

            return response.setComplete();
        }

        return chain.filter(exchange);
    }
}
```

逻辑：

```
请求 → Gateway
        ↓
检查 token
        ↓
合法 → 转发
非法 → 401
```

---

# 八、日志过滤器

```
LoggingFilter
```

```java
@Component
public class LoggingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long start = System.currentTimeMillis();

        return chain.filter(exchange).then(
            Mono.fromRunnable(() -> {
                long time = System.currentTimeMillis() - start;
                System.out.println("请求耗时：" + time);
            })
        );
    }
}
```

---

# 九、跨域配置

浏览器必须：

```
config/CorsConfig
```

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
```

---

# 十、限流（企业级）

Spring Gateway 内置：

```
Redis RateLimiter
```

例如：

```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
```

含义：

```
每秒 10 个请求
最大 20
```

---

# 十一、异常统一处理

```
exception/GatewayExceptionHandler
```

```java
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        return response.setComplete();
    }
}
```

---

# 十二、单元测试

使用：

```
JUnit + WebTestClient
```

示例：

```java
@SpringBootTest
@AutoConfigureWebTestClient
class GatewayTest {

    @Autowired
    WebTestClient webClient;

    @Test
    void testGatewayRoute(){

        webClient.get()
                .uri("/api/auth/login")
                .exchange()
                .expectStatus()
                .isOk();
    }
}
```

---

# 十三、接口测试

使用：

```
Postman
```

测试：

### 1 路由

```
GET /api/auth/login
```

是否转发成功。

---

### 2 Token校验

测试：

```
不带token → 401
带token → 200
```

---

### 3 限流

压测：

```
JMeter
```

看是否触发限流。

---

# 十四、日志规范

建议使用：

```
slf4j + logback
```

不要用：

```
System.out.println
```

示例：

```java
private static final Logger log =
        LoggerFactory.getLogger(AuthFilter.class);

log.info("request path: {}", request.getURI());
```

---

# 十五、企业级网关功能清单

你只要做到这些就 **达标**：

| 功能    | 必须    |
| ----- | ----- |
| 路由转发  | ✅     |
| JWT认证 | ✅     |
| 日志    | ✅     |
| 跨域    | ✅     |
| 异常处理  | ✅     |
| 限流    | ⭐推荐   |
| 链路追踪  | ⭐可选   |
| 灰度发布  | ❌不是必须 |

---

# 十六、最终项目结构（推荐）

```
gateway-service
│
├── config
│   ├── CorsConfig
│
├── filter
│   ├── AuthFilter
│   ├── LoggingFilter
│
├── route
│   ├── RouteLocatorConfig
│
├── exception
│   ├── GatewayExceptionHandler
│
├── util
│   ├── JwtUtil
│
└── GatewayApplication
```

---

# 十七、开发顺序（非常重要）

正确顺序：

```
1 创建项目
2 写路由配置
3 测试转发
4 写认证过滤器
5 写日志过滤器
6 写跨域
7 写限流
8 写异常处理
9 写单元测试
10 接入 auth-service
```

---

# 十八、给你的真实建议

你现在在做一个 **IM系统微服务**。

标准架构应该是：

```
gateway-service
auth-service
user-service
im-service
message-service
file-service
```

**gateway 是最先搭好的骨架。**

---

如果你愿意，我可以 **直接给你一套企业级 Gateway 完整代码骨架**（含过滤器 + 路由 + JWT + 限流 + 测试），你可以直接放进你的 IM 微服务项目里用。
我也可以顺便告诉你 **面试官最喜欢问的 Gateway 8 个问题**。
