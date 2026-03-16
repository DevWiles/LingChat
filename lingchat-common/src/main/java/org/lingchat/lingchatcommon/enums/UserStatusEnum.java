package org.lingchat.lingchatcommon.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {

    ONLINE(1, "在线"),
    OFFLINE(0, "离线"),
    AWAY(2, "离开"),
    DO_NOT_DISTURB(3, "勿扰"),
    INVISIBLE(4, "隐身");


    private final Integer status;
    private final String description;

    UserStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }
}
