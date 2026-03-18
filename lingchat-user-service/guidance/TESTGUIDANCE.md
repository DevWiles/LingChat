我来帮你分析如何测试这个用户服务模块。首先让我查看一下现有的测试文件和项目结构。




**已查看文件** *LingchatUserServiceApplicationTests.java*

**已查看文件** *pom.xml*

**已查看文件** *application.yaml*


现在让我查看一下主要的业务代码，以便提供更具体的测试建议：




**已查看文件** *FriendService.java*

**已查看文件** *UserService.java*

**已查看文件** *UserController.java*


根据代码分析，我为你提供以下测试方案：

## 如何测试这个用户服务模块

### 1. **单元测试**（推荐创建以下测试类）

#### 1.1 Service 层单元测试

**UserService 测试** - 使用 Mockito 模拟 Repository：
- `getUserProfile()` - 测试获取用户资料
- `updateUserProfile()` - 测试更新用户资料
- `updateOnlineStatus()` - 测试更新在线状态

**FriendService 测试** - 使用 Mockito 模拟 Repository：
- `sendFriendRequest()` - 测试发送好友请求
- `handleFriendRequest()` - 测试处理好友请求（同意/拒绝）
- `getFriendList()` - 测试获取好友列表
- `deleteFriend()` - 测试删除好友
- `setFriendRemark()` - 测试设置好友备注
- `addToBlacklist()` / `removeFromBlacklist()` - 测试黑名单功能
- `getPendingRequests()` - 测试获取待处理请求

#### 1.2 Controller 层单元测试
测试 REST API 端点，验证请求映射、参数验证和响应格式。

### 2. **集成测试**

使用 `@SpringBootTest` 进行完整的集成测试：
- 使用 H2 内存数据库或 Testcontainers 启动测试数据库
- 测试完整的业务流程
- 验证数据库操作的正确性

### 3. **API 接口测试**

使用 Postman、Apifox 或 curl 测试 HTTP 接口：

```bash
# 获取用户资料
GET http://localhost:8081/api/user/profile/1

# 更新用户资料
PUT http://localhost:8081/api/user/profile
Headers: X-User-Id: 1
Body: { "nickname": "测试用户", "avatar": "url" }

# 更新在线状态
PUT http://localhost:8081/api/user/status?statusCode=1
Headers: X-User-Id: 1
```


### 4. **具体测试示例**

我可以帮你创建测试文件。例如，创建一个 UserService 的单元测试：

```java
// UserRepository 测试示例
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Test
    void testGetUserProfile() {
        // 准备测试数据
        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setNickname("测试用户");
        userProfileRepository.save(profile);
        
        // 执行测试
        UserProfileResponse response = userService.getUserProfile(1L);
        
        // 验证结果
        assertEquals("测试用户", response.getNickname());
    }
}
```


### 5. **运行测试**

```bash
# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=UserServiceTest

# 运行并生成测试报告
./mvnw clean test jacoco:report
```


### 6. **测试注意事项**

- ✅ 需要配置测试数据库（可以使用 H2 或 MySQL 测试实例）
- ✅ Redis 连接配置（测试环境可使用嵌入式 Redis）
- ✅ 清理测试数据，避免污染生产数据
- ✅ 测试边界条件和异常情况

**你想要我帮你创建哪些具体的测试文件？** 比如：
- Service 层的单元测试（使用 Mockito）
- Controller 层的集成测试
- 完整的集成测试示例