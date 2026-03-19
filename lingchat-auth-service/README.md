# 注册、登录认证模块
## 功能：
1. 注册
2. 登录

## 目录结构
    
    authService/
    ├── config/                          # 配置类
    │   ├── SecurityConfig.java         # Spring Security 配置
    │   ├── JwtConfig.java              # JWT 令牌配置
    │   └── RedisConfig.java            # Redis 缓存配置（用于 token 存储）
    │
    ├── controller/                      # 控制器层
    │   ├── AuthController.java         # 认证控制器（注册、登录）
    │   └── UserController.java         # 用户信息控制器
    │
    ├── service/                         # 业务逻辑层
    │   ├── AuthService.java            # 认证服务接口
    │   ├── AuthServiceImpl.java        # 认证服务实现
    │   ├── UserService.java            # 用户服务接口
    │   └── UserServiceImpl.java        # 用户服务实现
    │
    ├── repository/                      # 数据访问层
    │   └── UserRepository.java         # 用户数据访问接口
    │
    ├── entity/                          # 实体类
    │   └── User.java                   # 用户实体
    │
    ├── dto/                             # 数据传输对象
    │   ├── request/                     # 请求 DTO
    │   │   ├── RegisterRequest.java    # 注册请求
    │   │   └── LoginRequest.java       # 登录请求
    │   └── response/                    # 响应 DTO
    │       ├── AuthResponse.java       # 认证响应
    │       └── UserInfoResponse.java   # 用户信息响应
    │
    ├── security/                        # 安全相关
    │   ├── JwtTokenProvider.java       # JWT 令牌生成与验证
    │   ├── UserDetailsImpl.java        # Spring Security 用户详情
    │   └── AuthenticationFilter.java   # 认证过滤器
    │
    ├── exception/                       # 异常处理
    │   ├── AuthException.java          # 认证异常
    │   └── GlobalExceptionHandler.java # 全局异常处理器
    │
    └── README.md                        # 模块说明文档
    
## 模块介绍
1. controller 层 - HTTP 接口入口
   - AuthController: 处理注册、登录、登出等请求
   - UserController: 处理用户信息查询、修改等请求
---
2. service 层 - 业务逻辑
   - AuthService: 定义认证相关方法（注册、登录、token 刷新等）
   - UserService: 定义用户管理相关方法
---
3. repository 层 - 数据访问
   - UserRepository: 操作用户表数据
---
4. entity 层 - 数据库实体映射
   - User: 对应 user 表
---
5. dto 层 - 数据传输对象
   - 分离请求和响应的数据结构，保证接口规范性
---
6. security 层 - 安全认证核心
   - JwtTokenProvider: JWT token 的生成、解析、验证
   - AuthenticationFilter: 请求拦截和身份验证
---
7. config 层 - 配置类
   - Spring Security、JWT、Redis 等相关配置
---
8. exception 层 - 异常处理
   - 统一的异常处理和错误响应
