package org.lingchat.lingchatuserservice.dto.request;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    private String nickname;

    private String avatar;

    private String signature;
}