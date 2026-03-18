package org.lingchat.lingchatuserservice.dto.request;

import lombok.Data;

@Data
public class FriendRemarkRequest {

    private Long friendId;

    private String remark;
}
