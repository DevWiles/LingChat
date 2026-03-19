package org.lingchat.lingchatuserservice.dto.response;

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

    private String signature;
}
