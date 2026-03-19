package org.lingchat.lingchatuserservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lingchat.lingchatcommon.enums.UserStatusEnum;
import org.lingchat.lingchatuserservice.dto.request.FriendAddRequest;
import org.lingchat.lingchatuserservice.dto.request.FriendRemarkRequest;
import org.lingchat.lingchatuserservice.dto.response.FriendInfoResponse;
import org.lingchat.lingchatuserservice.dto.response.FriendRequestResponse;
import org.lingchat.lingchatuserservice.entity.FriendRequest;
import org.lingchat.lingchatuserservice.entity.Friendship;
import org.lingchat.lingchatuserservice.entity.UserProfile;
import org.lingchat.lingchatuserservice.repository.FriendRequestRepository;
import org.lingchat.lingchatuserservice.repository.FriendshipRepository;
import org.lingchat.lingchatuserservice.repository.UserProfileRepository;
import org.lingchat.lingchatuserservice.service.impl.FriendServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FriendService 单元测试")
class FriendServiceTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private FriendServiceImpl friendService;

    private UserProfile user1;
    private UserProfile user2;
    private FriendRequest friendRequest;
    private Friendship friendship;

    @BeforeEach
    void setUp() {
        // 准备用户 1
        user1 = new UserProfile();
        user1.setUserId(1L);
        user1.setNickName("用户 1");
        user1.setAvatar("http://example.com/avatar1.jpg");
        user1.setSignature("用户 1 的签名");

        // 准备用户 2
        user2 = new UserProfile();
        user2.setUserId(2L);
        user2.setNickName("用户 2");
        user2.setAvatar("http://example.com/avatar2.jpg");
        user2.setSignature("用户 2 的签名");

        // 准备好友请求
        friendRequest = new FriendRequest();
        friendRequest.setId(100L);
        friendRequest.setSenderId(1L);
        friendRequest.setReceiverId(2L);
        friendRequest.setMessage("你好，我想加你为好友");
        friendRequest.setStatus(0); // 待处理
        friendRequest.setCreateTime(LocalDateTime.now());

        // 准备好友关系
        friendship = new Friendship();
        friendship.setId(1L);
        friendship.setUserId(1L);
        friendship.setFriendId(2L);
        friendship.setRemark("好朋友");
        friendship.setGroupId(0);
        friendship.setIsBlacklisted(false);
        friendship.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试发送好友请求 - 成功场景")
    void testSendFriendRequest_Success() {
        // 准备
        FriendAddRequest request = new FriendAddRequest();
        request.setFriendId(2L);
        request.setMessage("你好，我想加你为好友");

        when(friendshipRepository.existsByUserIdAndFriendId(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.findAll()).thenReturn(new ArrayList<>());
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(friendRequest);

        // 执行
        assertDoesNotThrow(() -> friendService.sendFriendRequest(1L, request));

        // 验证
        verify(friendRequestRepository, times(1)).save(any(FriendRequest.class));
        verify(friendshipRepository, times(1)).existsByUserIdAndFriendId(1L, 2L);
    }

    @Test
    @DisplayName("测试发送好友请求 - 添加自己为好友")
    void testSendFriendRequest_AddSelf() {
        // 准备
        FriendAddRequest request = new FriendAddRequest();
        request.setFriendId(1L); // 自己
        request.setMessage("加自己为好友");

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            friendService.sendFriendRequest(1L, request)
        );
        assertEquals("不能添加自己为好友", exception.getMessage());
        
        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("测试发送好友请求 - 已是好友")
    void testSendFriendRequest_AlreadyFriends() {
        // 准备
        FriendAddRequest request = new FriendAddRequest();
        request.setFriendId(2L);
        request.setMessage("再次发送请求");

        when(friendshipRepository.existsByUserIdAndFriendId(1L, 2L)).thenReturn(true);

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            friendService.sendFriendRequest(1L, request)
        );
        assertEquals("对方已经是你的好友", exception.getMessage());
        
        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("测试发送好友请求 - 重复发送")
    void testSendFriendRequest_DuplicateRequest() {
        // 准备
        FriendAddRequest request = new FriendAddRequest();
        request.setFriendId(2L);
        request.setMessage("重复请求");

        List<FriendRequest> existingRequests = new ArrayList<>();
        FriendRequest existingRequest = new FriendRequest();
        existingRequest.setSenderId(1L);
        existingRequest.setReceiverId(2L);
        existingRequest.setStatus(0); // 待处理
        existingRequests.add(existingRequest);

        when(friendshipRepository.existsByUserIdAndFriendId(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.findAll()).thenReturn(existingRequests);

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            friendService.sendFriendRequest(1L, request)
        );
        assertEquals("已发送过好友申请，请勿重复发送", exception.getMessage());
        
        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("测试处理好友请求 - 同意")
    void testHandleFriendRequest_Agree() {
        // 准备
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

        // 执行
        assertDoesNotThrow(() -> friendService.handleFriendRequest(2L, 100L, true));

        // 验证
        verify(friendRequestRepository, times(1)).findById(100L);
        verify(friendshipRepository, times(2)).save(any(Friendship.class));
        verify(friendRequestRepository, times(1)).save(friendRequest);
        assertEquals(1, friendRequest.getStatus()); // 已同意
    }

    @Test
    @DisplayName("测试处理好友请求 - 拒绝")
    void testHandleFriendRequest_Reject() {
        // 准备
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        // 执行
        assertDoesNotThrow(() -> friendService.handleFriendRequest(2L, 100L, false));

        // 验证
        verify(friendRequestRepository, times(1)).findById(100L);
        verify(friendshipRepository, never()).save(any(Friendship.class));
        verify(friendRequestRepository, times(1)).save(friendRequest);
        assertEquals(2, friendRequest.getStatus()); // 已拒绝
    }

    @Test
    @DisplayName("测试处理好友请求 - 请求不存在")
    void testHandleFriendRequest_RequestNotFound() {
        // 准备
        when(friendRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            friendService.handleFriendRequest(2L, 999L, true)
        );
        assertEquals("好友申请不存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试处理好友请求 - 无权处理")
    void testHandleFriendRequest_NoPermission() {
        // 准备
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            friendService.handleFriendRequest(3L, 100L, true) // 3L 不是接收者
        );
        assertEquals("无权处理该好友申请", exception.getMessage());
    }

    @Test
    @DisplayName("测试处理好友请求 - 已处理的请求")
    void testHandleFriendRequest_AlreadyProcessed() {
        // 准备
        friendRequest.setStatus(1); // 已同意
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            friendService.handleFriendRequest(2L, 100L, true)
        );
        assertEquals("该申请已处理", exception.getMessage());
    }

    @Test
    @DisplayName("测试获取好友列表 - 成功场景")
    void testGetFriendList_Success() {
        // 准备
        List<Friendship> friendships = new ArrayList<>();
        friendships.add(friendship);

        when(friendshipRepository.findByUserIdAndIsBlacklistedFalse(1L)).thenReturn(friendships);
        when(userProfileRepository.findByUserId(2L)).thenReturn(Optional.of(user2));

        // 执行
        List<FriendInfoResponse> result = friendService.getFriendList(1L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        FriendInfoResponse response = result.get(0);
        assertEquals(2L, response.getUserId());
        assertEquals("用户 2", response.getNickname());
        assertEquals("好朋友", response.getRemark());

        verify(friendshipRepository, times(1)).findByUserIdAndIsBlacklistedFalse(1L);
        verify(userProfileRepository, times(1)).findByUserId(2L);
    }

    @Test
    @DisplayName("测试获取好友列表 - 空列表")
    void testGetFriendList_Empty() {
        // 准备
        when(friendshipRepository.findByUserIdAndIsBlacklistedFalse(1L)).thenReturn(new ArrayList<>());

        // 执行
        List<FriendInfoResponse> result = friendService.getFriendList(1L);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试删除好友 - 成功场景")
    void testDeleteFriend_Success() {
        // 准备
        Friendship reverseFriendship = new Friendship();
        reverseFriendship.setId(2L);
        reverseFriendship.setUserId(2L);
        reverseFriendship.setFriendId(1L);

        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.of(reverseFriendship));

        // 执行
        assertDoesNotThrow(() -> friendService.deleteFriend(1L, 2L));

        // 验证
        verify(friendshipRepository, times(1)).delete(friendship);
        verify(friendshipRepository, times(1)).delete(reverseFriendship);
    }

    @Test
    @DisplayName("测试删除好友 - 仅单向关系")
    void testDeleteFriend_SingleDirection() {
        // 准备
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.empty());

        // 执行
        assertDoesNotThrow(() -> friendService.deleteFriend(1L, 2L));

        // 验证
        verify(friendshipRepository, times(1)).delete(friendship);
        verify(friendshipRepository, times(1)).findByUserIdAndFriendId(2L, 1L);
    }

    @Test
    @DisplayName("测试删除好友 - 关系不存在")
    void testDeleteFriend_NotFound() {
        // 准备
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.empty());

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            friendService.deleteFriend(1L, 2L)
        );
        assertEquals("好友关系不存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试设置好友备注 - 成功场景")
    void testSetFriendRemark_Success() {
        // 准备
        FriendRemarkRequest request = new FriendRemarkRequest();
        request.setFriendId(2L);
        request.setRemark("最好的朋友");

        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(friendship));

        // 执行
        assertDoesNotThrow(() -> friendService.setFriendRemark(1L, request));

        // 验证
        assertEquals("最好的朋友", friendship.getRemark());
        verify(friendshipRepository, times(1)).save(friendship);
    }

    @Test
    @DisplayName("测试设置好友备注 - 关系不存在")
    void testSetFriendRemark_NotFound() {
        // 准备
        FriendRemarkRequest request = new FriendRemarkRequest();
        request.setFriendId(999L);

        when(friendshipRepository.findByUserIdAndFriendId(1L, 999L)).thenReturn(Optional.empty());

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            friendService.setFriendRemark(1L, request)
        );
        assertEquals("好友关系不存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试添加到黑名单 - 成功场景")
    void testAddToBlacklist_Success() {
        // 准备
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(friendship));

        // 执行
        assertDoesNotThrow(() -> friendService.addToBlacklist(1L, 2L));

        // 验证
        assertTrue(friendship.getIsBlacklisted());
        verify(friendshipRepository, times(1)).save(friendship);
    }

    @Test
    @DisplayName("测试添加到黑名单 - 关系不存在")
    void testAddToBlacklist_NotFound() {
        // 准备
        when(friendshipRepository.findByUserIdAndFriendId(1L, 999L)).thenReturn(Optional.empty());

        // 执行 & 验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            friendService.addToBlacklist(1L, 999L)
        );
        assertEquals("好友关系不存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试从黑名单移除 - 成功场景")
    void testRemoveFromBlacklist_Success() {
        // 准备
        friendship.setIsBlacklisted(true);
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(friendship));

        // 执行
        assertDoesNotThrow(() -> friendService.removeFromBlacklist(1L, 2L));

        // 验证
        assertFalse(friendship.getIsBlacklisted());
        verify(friendshipRepository, times(1)).save(friendship);
    }

    @Test
    @DisplayName("测试获取待处理的好友请求 - 成功场景")
    void testGetPendingRequests_Success() {
        // 准备
        List<FriendRequest> requests = new ArrayList<>();
        requests.add(friendRequest);

        when(friendRequestRepository.findByReceiverIdAndStatus(2L, 0)).thenReturn(requests);
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(user1));

        // 执行
        List<FriendRequestResponse> result = friendService.getPendingRequests(2L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        FriendRequestResponse response = result.get(0);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getSenderId());
        assertEquals("用户 1", response.getSenderNickname());
        assertEquals("你好，我想加你为好友", response.getMessage());

        verify(friendRequestRepository, times(1)).findByReceiverIdAndStatus(2L, 0);
    }

    @Test
    @DisplayName("测试获取待处理的好友请求 - 空列表")
    void testGetPendingRequests_Empty() {
        // 准备
        when(friendRequestRepository.findByReceiverIdAndStatus(2L, 0)).thenReturn(new ArrayList<>());

        // 执行
        List<FriendRequestResponse> result = friendService.getPendingRequests(2L);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
