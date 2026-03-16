package org.lingchat.lingchatcommon.constant;

public class RedisKeyConstant {

    private static final String PREFIX = "lingchat:";

    // 用户相关
    public static final String USER_TOKEN = PREFIX + "user:token:%s";
    public static final String USER_INFO = PREFIX + "user:info:%s";
    public static final String USER_STATUS = PREFIX + "user:status:%s";

    // 会话相关
    public static final String USER_SESSION = PREFIX + "session:user:%s";
    public static final String USER_SESSION_MAP = PREFIX + "session:id:%s";

    // 消息相关
    public static final String UNREAD_COUNT = PREFIX + "message:unread:%s";
    public static final String MESSAGE_CACHE = PREFIX + "message:cache:%s";

    // 群组相关
    public static final String GROUP_MEMBERS = PREFIX + "group:members:%s";

    private RedisKeyConstant() {
        throw new IllegalStateException("Constant class");
    }
}
