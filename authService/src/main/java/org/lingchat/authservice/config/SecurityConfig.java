package org.lingchat.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置
 */
@Configuration
public class SecurityConfig {

    /**
     * 密码加密器
     * BCrypt 加密算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 关闭Spring Security 的 CSRF 保护，确保 api 访问不被 CSRF 攻击
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 关闭 CSRF 保护
            .csrf(csrf -> csrf.disable())

            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
            // 放行认证相关接口
                    .requestMatchers("/api/auth/**").permitAll()

                    // 剩余的接口需要认证
                    .anyRequest().authenticated()
            )

            // 配置会话管理为无状态（REST API）
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
