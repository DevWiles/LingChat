package org.lingchat.authservice.config;

import org.lingchat.authservice.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码加密器
     * BCrypt 加密算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 关闭 Spring Security 的 CSRF 保护，确保 api 访问不被 CSRF 攻击
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF 保护
                .csrf(csrf -> csrf.disable())

                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 放行认证相关接口（登录、注册无需 token）
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                        // 其他/api/auth/**路径需要认证
                        .requestMatchers("/api/auth/**").authenticated()

                        // 剩余的接口需要认证
                        .anyRequest().authenticated()
                )

                // 配置会话管理为无状态（REST API）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 添加 JWT 认证过滤器（在用户名密码认证过滤器之前执行）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 暴露 AuthenticationManager Bean，用于手动触发认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
