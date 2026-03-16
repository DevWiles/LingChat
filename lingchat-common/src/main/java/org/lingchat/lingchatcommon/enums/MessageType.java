package org.lingchat.lingchatcommon.enums;

import lombok.Getter;

@Getter
public enum MessageType {

    TEXT(1, "文本消息"),
    IMAGE(2, "图片消息"),
    FILE(3, "文件消息"),
    VOICE(4, "语音消息"),
    VIDEO(5, "视频消息"),
    SYSTEM(99, "系统消息");

    private final Integer type;
    private final String description;

    MessageType(Integer type, String description) {
        this.type = type;
        this.description = description;
    }
}
