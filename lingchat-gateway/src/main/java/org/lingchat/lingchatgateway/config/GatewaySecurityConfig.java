package org.lingchat.lingchatgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                // 不在 Security 中配置 CORS，使用单独的 CorsWebFilter
                .authorizeExchange(exchanges -> exchanges
                        // 放行所有认证相关的接口（注册、登录等）
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        // 其他所有请求都需要认证
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }
}
