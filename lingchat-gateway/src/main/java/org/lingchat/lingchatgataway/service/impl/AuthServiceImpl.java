package org.lingchat.lingchatgataway.service.impl;

import org.lingchat.lingchatgataway.service.AuthService;
import org.lingchat.lingchatgataway.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 简单的 token 格式检查
     * 注意：详细的 JWT 验证（签名、有效期、黑名单等）由 auth-service 完成
     */
    @Override
    public boolean isTokenFormatValid(String token) {
        return jwtUtils.isTokenFormatValid(token);
    }
}
