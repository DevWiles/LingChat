package org.lingchat.lingchatuserservice.dto.request;

import lombok.Data;

@Data
public class FriendAddRequest {

    private Long friendId;

    private String message;
}
