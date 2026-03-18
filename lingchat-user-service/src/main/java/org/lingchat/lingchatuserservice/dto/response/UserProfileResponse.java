package org.lingchat.lingchatuserservice.dto.response;

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
