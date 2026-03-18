好的！我来帮你规划并创建 **用户服务模块 (lingchat-user-service)**，实现用户信息管理和好友关系功能。

首先让我查看一下现有的项目结构和公共模块的内容：




**已查看文件** *pom.xml*

**已查看文件** *pom.xml*

**已查看目录** *lingchat-common/src/main/java/org/lingchat/lingchatcommon* 7 个结果


现在让我查看一下公共模块中的枚举和模型类，以便更好地设计用户服务：




**已查看目录** *lingchat-common/src/main/java/org/lingchat/lingchatcommon/enums* 3 个结果

**已查看文件** *UserStatusEnum.java*

**已查看文件** *Result.java*


完美！现在我来帮你创建 **用户服务模块 (lingchat-user-service)**。我将按照 DDD 分层架构，实现以下功能：

## 📋 功能规划

### 1. 用户信息管理
- 获取/修改用户资料（昵称、头像、签名）
- 设置在线状态
- 用户详情查询

### 2. 好友关系管理
- 添加好友（申请/接受/拒绝）
- 删除好友
- 获取好友列表
- 好友备注名
- 黑名单功能

---

现在开始创建代码：

## 第一步：更新父 POM，添加 user-service 模块

```xml
    <modules>
        <module>lingchat-auth-service</module>
        <module>lingchat-common</module>
        <module>lingchat-gateway</module>
+       <module>lingchat-user-service</module>
    </modules>
```


---

## 第二步：创建用户服务目录结构和代码

### 1. 创建 `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.lingchat</groupId>
        <artifactId>lingchat</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>lingchat-user-service</artifactId>
    <name>lingchat-user-service</name>
    <description>User Service - User profile and friend management</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- 公共模块 -->
        <dependency>
            <groupId>org.lingchat</groupId>
            <artifactId>lingchat-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- MySQL 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Cloud Nacos (服务注册与发现) -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
            <version>2023.0.1.0</version>
        </dependency>

        <!-- OpenFeign (服务间调用) -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```


---

### 2. 创建应用配置文件 `application.yaml`

```yaml
spring:
  application:
    name: user-service
  
  datasource:
    url: jdbc:mysql://localhost:3306/lingchat_user?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
  
  redis:
    host: localhost
    port: 6379
    database: 1
    timeout: 5000ms

server:
  port: 8081

# Nacos 配置（暂时注释，后续配置）
# spring:
#   cloud:
#     nacos:
#       discovery:
#         server-addr: localhost:8848

logging:
  level:
    org.lingchat.userservice: debug
    org.hibernate.SQL: debug
```


---

### 3. 创建主启动类 `UserServiceApplication.java`

```java
package org.lingchat.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```


---

### 4. 创建实体类

#### `entity/UserProfile.java` - 用户档案实体

```java
package org.lingchat.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.lingchat.lingchatcommon.enums.UserStatusEnum;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    private Long userId;

    @Column(length = 50)
    private String nickname;

    @Column(length = 200)
    private String avatar;

    @Column(length = 500)
    private String signature;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private UserStatusEnum status = UserStatusEnum.OFFLINE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
```


#### `entity/FriendRequest.java` - 好友申请实体

```java
package org.lingchat.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "friend_request")
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long receiverId;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private Integer status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
}
```


#### `entity/Friendship.java` - 好友关系实体

```java
package org.lingchat.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "friendship", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userId", "friendId"})
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long friendId;

    @Column(length = 50)
    private String remark;

    @Column(nullable = false)
    private Integer groupId;

    @Column(nullable = false)
    private Boolean isBlacklisted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
}
```


---

### 5. 创建 DTO 类

#### `dto/request/UserProfileUpdateRequest.java`

```java
package org.lingchat.userservice.dto.request;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    
    private String nickname;
    
    private String avatar;
    
    private String signature;
}
```


#### `dto/request/FriendAddRequest.java`

```java
package org.lingchat.userservice.dto.request;

import lombok.Data;

@Data
public class FriendAddRequest {
    
    private Long friendId;
    
    private String message;
}
```


#### `dto/request/FriendRemarkRequest.java`

```java
package org.lingchat.userservice.dto.request;

import lombok.Data;

@Data
public class FriendRemarkRequest {
    
    private Long friendId;
    
    private String remark;
}
```


#### `dto/response/UserProfileResponse.java`

```java
package org.lingchat.userservice.dto.response;

import lombok.Builder;
import lombok.Data;
import org.lingchat.lingchatcommon.enums.UserStatusEnum;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    
    private Long userId;
    
    private String username;
    
    private String nickname;
    
    private String avatar;
    
    private String signature;
    
    private UserStatusEnum status;
    
    private LocalDateTime createTime;
}
```


#### `dto/response/FriendInfoResponse.java`

```java
package org.lingchat.userservice.dto.response;

import lombok.Builder;
import lombok.Data;
import org.lingchat.lingchatcommon.enums.UserStatusEnum;

@Data
@Builder
public class FriendInfoResponse {
    
    private Long userId;
    
    private String username;
    
    private String nickname;
    
    private String avatar;
    
    private String remark;
    
    private UserStatusEnum status;
    
    private String signature;
}
```


#### `dto/response/FriendRequestResponse.java`

```java
package org.lingchat.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FriendRequestResponse {
    
    private Long id;
    
    private Long senderId;
    
    private String senderNickname;
    
    private String message;
    
    private Integer status;
    
    private LocalDateTime createTime;
}
```


---

### 6. 创建 Repository 层

#### `repository/UserProfileRepository.java`

```java
package org.lingchat.userservice.repository;

import org.lingchat.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByUserId(Long userId);
}
```


#### `repository/FriendshipRepository.java`

```java
package org.lingchat.userservice.repository;

import org.lingchat.userservice.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    List<Friendship> findByUserIdAndIsBlacklistedFalse(Long userId);
    
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
    
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
    
    @Query("SELECT f FROM Friendship f WHERE f.userId = :userId AND f.friendId = :friendId OR f.userId = :friendId AND f.friendId = :userId")
    Optional<Friendship> findFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
```


#### `repository/FriendRequestRepository.java`

```java
package org.lingchat.userservice.repository;

import org.lingchat.userservice.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, Integer status);
    
    List<FriendRequest> findBySenderIdAndStatus(Long senderId, Integer status);
}
```


---

### 7. 创建 Service 层

#### `service/UserService.java`

```java
package org.lingchat.userservice.service;

import org.lingchat.userservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.userservice.dto.response.UserProfileResponse;

public interface UserService {
    
    UserProfileResponse getUserProfile(Long userId);
    
    UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);
    
    void updateOnlineStatus(Long userId, Integer statusCode);
}
```


#### `service/impl/UserServiceImpl.java`

```java
package org.lingchat.userservice.service.impl;

import org.lingchat.lingchatcommon.enums.UserStatusEnum;
import org.lingchat.userservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.userservice.dto.response.UserProfileResponse;
import org.lingchat.userservice.entity.UserProfile;
import org.lingchat.userservice.repository.UserProfileRepository;
import org.lingchat.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户档案不存在"));
        
        return convertToResponse(profile);
    }
    
    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(userId);
                    return userProfileRepository.save(newProfile);
                });
        
        if (request.getNickname() != null) {
            profile.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            profile.setAvatar(request.getAvatar());
        }
        if (request.getSignature() != null) {
            profile.setSignature(request.getSignature());
        }
        
        return convertToResponse(userProfileRepository.save(profile));
    }
    
    @Override
    @Transactional
    public void updateOnlineStatus(Long userId, Integer statusCode) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户档案不存在"));
        
        UserStatusEnum status = switch (statusCode) {
            case 0 -> UserStatusEnum.OFFLINE;
            case 1 -> UserStatusEnum.ONLINE;
            case 2 -> UserStatusEnum.AWAY;
            case 3 -> UserStatusEnum.DO_NOT_DISTURB;
            case 4 -> UserStatusEnum.INVISIBLE;
            default -> throw new IllegalArgumentException("无效的状态码");
        };
        
        profile.setStatus(status);
        userProfileRepository.save(profile);
    }
    
    private UserProfileResponse convertToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .userId(profile.getUserId())
                .nickname(profile.getNickname())
                .avatar(profile.getAvatar())
                .signature(profile.getSignature())
                .status(profile.getStatus())
                .createTime(profile.getCreateTime())
                .build();
    }
}
```


#### `service/FriendService.java`

```java
package org.lingchat.userservice.service;

import org.lingchat.userservice.dto.request.FriendAddRequest;
import org.lingchat.userservice.dto.request.FriendRemarkRequest;
import org.lingchat.userservice.dto.response.FriendInfoResponse;
import org.lingchat.userservice.dto.response.FriendRequestResponse;

import java.util.List;

public interface FriendService {
    
    void sendFriendRequest(Long userId, FriendAddRequest request);
    
    void handleFriendRequest(Long userId, Long requestId, Boolean agree);
    
    List<FriendInfoResponse> getFriendList(Long userId);
    
    void deleteFriend(Long userId, Long friendId);
    
    void setFriendRemark(Long userId, FriendRemarkRequest request);
    
    void addToBlacklist(Long userId, Long friendId);
    
    void removeFromBlacklist(Long userId, Long friendId);
    
    List<FriendRequestResponse> getPendingRequests(Long userId);
}
```


#### `service/impl/FriendServiceImpl.java`

```java
package org.lingchat.userservice.service.impl;

import org.lingchat.userservice.dto.request.FriendAddRequest;
import org.lingchat.userservice.dto.request.FriendRemarkRequest;
import org.lingchat.userservice.dto.response.FriendInfoResponse;
import org.lingchat.userservice.dto.response.FriendRequestResponse;
import org.lingchat.userservice.entity.FriendRequest;
import org.lingchat.userservice.entity.Friendship;
import org.lingchat.userservice.entity.UserProfile;
import org.lingchat.userservice.repository.FriendRequestRepository;
import org.lingchat.userservice.repository.FriendshipRepository;
import org.lingchat.userservice.repository.UserProfileRepository;
import org.lingchat.userservice.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendServiceImpl implements FriendService {
    
    @Autowired
    private FriendRequestRepository friendRequestRepository;
    
    @Autowired
    private FriendshipRepository friendshipRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Override
    @Transactional
    public void sendFriendRequest(Long userId, FriendAddRequest request) {
        if (userId.equals(request.getFriendId())) {
            throw new RuntimeException("不能添加自己为好友");
        }
        
        if (friendshipRepository.existsByUserIdAndFriendId(userId, request.getFriendId())) {
            throw new RuntimeException("对方已经是你的好友");
        }
        
        FriendRequest existingRequest = friendRequestRepository.findAll()
                .stream()
                .filter(r -> r.getSenderId().equals(userId) && r.getReceiverId().equals(request.getFriendId()) && r.getStatus() == 0)
                .findFirst()
                .orElse(null);
        
        if (existingRequest != null) {
            throw new RuntimeException("已发送过好友申请，请勿重复发送");
        }
        
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userId);
        friendRequest.setReceiverId(request.getFriendId());
        friendRequest.setMessage(request.getMessage());
        friendRequest.setStatus(0);
        
        friendRequestRepository.save(friendRequest);
    }
    
    @Override
    @Transactional
    public void handleFriendRequest(Long userId, Long requestId, Boolean agree) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("好友申请不存在"));
        
        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权处理该好友申请");
        }
        
        if (request.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }
        
        if (agree) {
            Friendship friendship1 = new Friendship();
            friendship1.setUserId(userId);
            friendship1.setFriendId(request.getSenderId());
            friendship1.setGroupId(0);
            friendship1.setIsBlacklisted(false);
            friendshipRepository.save(friendship1);
            
            Friendship friendship2 = new Friendship();
            friendship2.setUserId(request.getSenderId());
            friendship2.setFriendId(userId);
            friendship2.setGroupId(0);
            friendship2.setIsBlacklisted(false);
            friendshipRepository.save(friendship2);
            
            request.setStatus(1);
        } else {
            request.setStatus(2);
        }
        
        friendRequestRepository.save(request);
    }
    
    @Override
    public List<FriendInfoResponse> getFriendList(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserIdAndIsBlacklistedFalse(userId);
        
        List<FriendInfoResponse> result = new ArrayList<>();
        for (Friendship friendship : friendships) {
            UserProfile profile = userProfileRepository.findByUserId(friendship.getFriendId()).orElse(null);
            if (profile != null) {
                result.add(convertToFriendInfoResponse(profile, friendship.getRemark()));
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));
        
        friendshipRepository.delete(friendship);
        
        Friendship reverseFriendship = friendshipRepository.findByUserIdAndFriendId(friendId, userId)
                .orElse(null);
        if (reverseFriendship != null) {
            friendshipRepository.delete(reverseFriendship);
        }
    }
    
    @Override
    @Transactional
    public void setFriendRemark(Long userId, FriendRemarkRequest request) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, request.getFriendId())
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));
        
        friendship.setRemark(request.getRemark());
        friendshipRepository.save(friendship);
    }
    
    @Override
    @Transactional
    public void addToBlacklist(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));
        
        friendship.setIsBlacklisted(true);
        friendshipRepository.save(friendship);
    }
    
    @Override
    @Transactional
    public void removeFromBlacklist(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));
        
        friendship.setIsBlacklisted(false);
        friendshipRepository.save(friendship);
    }
    
    @Override
    public List<FriendRequestResponse> getPendingRequests(Long userId) {
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(userId, 0);
        
        return requests.stream()
                .map(this::convertToFriendRequestResponse)
                .toList();
    }
    
    private FriendInfoResponse convertToFriendInfoResponse(UserProfile profile, String remark) {
        return FriendInfoResponse.builder()
                .userId(profile.getUserId())
                .nickname(profile.getNickname())
                .avatar(profile.getAvatar())
                .remark(remark)
                .status(profile.getStatus())
                .signature(profile.getSignature())
                .build();
    }
    
    private FriendRequestResponse convertToFriendRequestResponse(FriendRequest request) {
        UserProfile senderProfile = userProfileRepository.findByUserId(request.getSenderId()).orElse(null);
        
        return FriendRequestResponse.builder()
                .id(request.getId())
                .senderId(request.getSenderId())
                .senderNickname(senderProfile != null ? senderProfile.getNickname() : "未知用户")
                .message(request.getMessage())
                .status(request.getStatus())
                .createTime(request.getCreateTime())
                .build();
    }
}
```


---

### 8. 创建 Controller 层

#### `controller/UserController.java`

```java
package org.lingchat.userservice.controller;

import org.lingchat.lingchatcommon.model.Result;
import org.lingchat.userservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.userservice.dto.response.UserProfileResponse;
import org.lingchat.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile/{userId}")
    public ResponseEntity<Result<UserProfileResponse>> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(Result.success(profile));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Result<UserProfileResponse>> updateUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse profile = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(Result.success(profile));
    }
    
    @PutMapping("/status")
    public ResponseEntity<Result<Void>> updateOnlineStatus(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Integer statusCode) {
        userService.updateOnlineStatus(userId, statusCode);
        return ResponseEntity.ok(Result.success());
    }
}
```


#### `controller/FriendController.java`

```java
package org.lingchat.userservice.controller;

import org.lingchat.lingchatcommon.model.Result;
import org.lingchat.userservice.dto.request.FriendAddRequest;
import org.lingchat.userservice.dto.request.FriendRemarkRequest;
import org.lingchat.userservice.dto.response.FriendInfoResponse;
import org.lingchat.userservice.dto.response.FriendRequestResponse;
import org.lingchat.userservice.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
public class FriendController {
    
    @Autowired
    private FriendService friendService;
    
    @PostMapping("/request")
    public ResponseEntity<Result<Void>> sendFriendRequest(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody FriendAddRequest request) {
        friendService.sendFriendRequest(userId, request);
        return ResponseEntity.ok(Result.success("好友申请已发送"));
    }
    
    @PostMapping("/request/handle")
    public ResponseEntity<Result<Void>> handleFriendRequest(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long requestId,
            @RequestParam Boolean agree) {
        friendService.handleFriendRequest(userId, requestId, agree);
        return ResponseEntity.ok(Result.success(agree ? "已同意好友申请" : "已拒绝好友申请"));
    }
    
    @GetMapping("/list")
    public ResponseEntity<Result<List<FriendInfoResponse>>> getFriendList(
            @RequestHeader("X-User-Id") Long userId) {
        List<FriendInfoResponse> friends = friendService.getFriendList(userId);
        return ResponseEntity.ok(Result.success(friends));
    }
    
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Result<Void>> deleteFriend(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long friendId) {
        friendService.deleteFriend(userId, friendId);
        return ResponseEntity.ok(Result.success("已删除好友"));
    }
    
    @PutMapping("/remark")
    public ResponseEntity<Result<Void>> setFriendRemark(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody FriendRemarkRequest request) {
        friendService.setFriendRemark(userId, request);
        return ResponseEntity.ok(Result.success("备注已更新"));
    }
    
    @PutMapping("/blacklist/{friendId}")
    public ResponseEntity<Result<Void>> addToBlacklist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long friendId) {
        friendService.addToBlacklist(userId, friendId);
        return ResponseEntity.ok(Result.success("已加入黑名单"));
    }
    
    @DeleteMapping("/blacklist/{friendId}")
    public ResponseEntity<Result<Void>> removeFromBlacklist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long friendId) {
        friendService.removeFromBlacklist(userId, friendId);
        return ResponseEntity.ok(Result.success("已移出黑名单"));
    }
    
    @GetMapping("/requests/pending")
    public ResponseEntity<Result<List<FriendRequestResponse>>> getPendingRequests(
            @RequestHeader("X-User-Id") Long userId) {
        List<FriendRequestResponse> requests = friendService.getPendingRequests(userId);
        return ResponseEntity.ok(Result.success(requests));
    }
}
```


---

### 9. 创建全局异常处理

#### `exception/GlobalExceptionHandler.java`

```java
package org.lingchat.userservice.exception;

import org.lingchat.lingchatcommon.model.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(Result.fail(e.getMessage()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Result.fail(e.getMessage()));
    }
}
```


---

## 📊 API 接口设计

### 用户信息接口
- `GET /api/user/profile/{userId}` - 获取用户资料
- `PUT /api/user/profile` - 修改用户资料
- `PUT /api/user/status?statusCode={code}` - 设置在线状态

### 好友管理接口
- `POST /api/friend/request` - 发送好友申请
- `POST /api/friend/request/handle?requestId={id}&agree={true/false}` - 处理好友申请
- `GET /api/friend/list` - 获取好友列表
- `DELETE /api/friend/{friendId}` - 删除好友
- `PUT /api/friend/remark` - 设置好友备注
- `PUT /api/friend/blacklist/{friendId}` - 加入黑名单
- `DELETE /api/friend/blacklist/{friendId}` - 移出黑名单
- `GET /api/friend/requests/pending` - 获取待处理的好友申请

---

## ✅ 下一步操作

1. **在 IDE 中创建文件结构**（按上述代码逐个创建）
2. **在父 pom.xml 中添加模块声明**
3. **启动数据库 MySQL**
4. **运行项目**：`mvn spring-boot:run`

需要我帮你创建 SQL 初始化脚本或者解释某个功能的实现细节吗？🎉