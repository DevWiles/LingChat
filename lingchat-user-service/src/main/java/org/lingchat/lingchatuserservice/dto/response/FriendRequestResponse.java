package org.lingchat.lingchatuserservice.dto.response;

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
