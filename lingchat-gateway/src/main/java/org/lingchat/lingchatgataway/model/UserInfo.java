package org.lingchat.lingchatgataway.model;

import lombok.Data;

/**
 * 网关传递的用户信息模型
 * 注意：只包含必要的用户标识信息，不包含业务字段
 */
@Data
public class UserInfo {
    /**
     * 用户 ID - 用于传递给后端服务进行身份识别
     */
    private String userId;
}
