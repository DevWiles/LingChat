package org.lingchat.lingchatuserservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lingchat.lingchatcommon.enums.UserStatusEnum;
import org.lingchat.lingchatuserservice.dto.request.UserProfileUpdateRequest;
import org.lingchat.lingchatuserservice.dto.response.UserProfileResponse;
import org.lingchat.lingchatuserservice.entity.UserProfile;
import org.lingchat.lingchatuserservice.repository.UserProfileRepository;
import org.lingchat.lingchatuserservice.service.impl.UserServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testProfile = new UserProfile();
        testProfile.setUserId(1L);
        testProfile.setNickName("测试用户");
        testProfile.setAvatar("http://example.com/avatar.jpg");
        testProfile.setSignature("这是我的签名");
        testProfile.setStatus(UserStatusEnum.OFFLINE);
        testProfile.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试获取用户资料 - 成功场景")
    void testGetUserProfile_Success() {
        // 准备
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        // 执行
        UserProfileResponse response = userService.getUserProfile(1L);

        // 验证
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("测试用户", response.getNickname());
        assertEquals("http://example.com/avatar.jpg", response.getAvatar());
        assertEquals("这是我的签名", response.getSignature());
        assertEquals(UserStatusEnum.OFFLINE, response.getStatus());

        // 验证方法调用
        verify(userProfileRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("测试获取用户资料 - 用户不存在")
    void testGetUserProfile_UserNotFound() {
        // 准备
        when(userProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            userService.getUserProfile(999L)
        );
        assertEquals("用户档案不存在", exception.getMessage());
        
        verify(userProfileRepository, times(1)).findByUserId(999L);
    }

    @Test
    @DisplayName("测试更新用户资料 - 成功场景")
    void testUpdateUserProfile_Success() {
        // 准备
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setNickname("新昵称");
        request.setAvatar("http://example.com/new-avatar.jpg");
        request.setSignature("新的签名");

        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setUserId(1L);
        updatedProfile.setNickName("新昵称");
        updatedProfile.setAvatar("http://example.com/new-avatar.jpg");
        updatedProfile.setSignature("新的签名");
        updatedProfile.setStatus(UserStatusEnum.OFFLINE);

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(updatedProfile);

        // 执行
        UserProfileResponse response = userService.updateUserProfile(1L, request);

        // 验证
        assertNotNull(response);
        assertEquals("新昵称", response.getNickname());
        assertEquals("http://example.com/new-avatar.jpg", response.getAvatar());
        assertEquals("新的签名", response.getSignature());

        verify(userProfileRepository, times(1)).findByUserId(1L);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("测试更新用户资料 - 用户不存在时创建新用户")
    void testUpdateUserProfile_CreateWhenNotExists() {
        // 准备
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setNickname("新用户");

        UserProfile newProfile = new UserProfile();
        newProfile.setUserId(2L);
        newProfile.setNickName("新用户");
        newProfile.setStatus(UserStatusEnum.OFFLINE);

        when(userProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(newProfile);

        // 执行
        UserProfileResponse response = userService.updateUserProfile(2L, request);

        // 验证
        assertNotNull(response);
        assertEquals("新用户", response.getNickname());
        assertEquals(2L, response.getUserId());

        verify(userProfileRepository, times(1)).findByUserId(2L);
        verify(userProfileRepository, times(2)).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("测试更新用户资料 - 部分字段更新")
    void testUpdateUserProfile_PartialUpdate() {
        // 准备
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setNickname("仅更新昵称");
        // avatar 和 signature 不设置

        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setUserId(1L);
        updatedProfile.setNickName("仅更新昵称");
        updatedProfile.setAvatar("http://example.com/avatar.jpg"); // 保持原值
        updatedProfile.setSignature("这是我的签名"); // 保持原值
        updatedProfile.setStatus(UserStatusEnum.OFFLINE);

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(updatedProfile);

        // 执行
        UserProfileResponse response = userService.updateUserProfile(1L, request);

        // 验证
        assertEquals("仅更新昵称", response.getNickname());
        // 验证未更新的字段保持不变
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("测试更新在线状态 - 成功场景")
    void testUpdateOnlineStatus_Success() {
        // 准备
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        // 执行
        userService.updateOnlineStatus(1L, 1); // ONLINE

        // 验证
        verify(userProfileRepository, times(1)).findByUserId(1L);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        assertEquals(UserStatusEnum.ONLINE, testProfile.getStatus());
    }

    @Test
    @DisplayName("测试更新在线状态 - 各种状态码")
    void testUpdateOnlineStatus_DifferentCodes() {
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        // 测试不同的状态码
        userService.updateOnlineStatus(1L, 0);
        assertEquals(UserStatusEnum.OFFLINE, testProfile.getStatus());

        userService.updateOnlineStatus(1L, 1);
        assertEquals(UserStatusEnum.ONLINE, testProfile.getStatus());

        userService.updateOnlineStatus(1L, 2);
        assertEquals(UserStatusEnum.AWAY, testProfile.getStatus());

        userService.updateOnlineStatus(1L, 3);
        assertEquals(UserStatusEnum.DO_NOT_DISTURB, testProfile.getStatus());

        userService.updateOnlineStatus(1L, 4);
        assertEquals(UserStatusEnum.INVISIBLE, testProfile.getStatus());
    }

    @Test
    @DisplayName("测试更新在线状态 - 无效状态码")
    void testUpdateOnlineStatus_InvalidCode() {
        // 准备
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            userService.updateOnlineStatus(1L, 99)
        );
        assertEquals("无效的状态码", exception.getMessage());
    }

    @Test
    @DisplayName("测试更新在线状态 - 用户不存在")
    void testUpdateOnlineStatus_UserNotFound() {
        // 准备
        when(userProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            userService.updateOnlineStatus(999L, 1)
        );
        assertEquals("用户档案不存在", exception.getMessage());
    }
}
