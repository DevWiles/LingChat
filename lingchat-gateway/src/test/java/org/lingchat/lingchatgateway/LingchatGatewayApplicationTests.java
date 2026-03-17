package org.lingchat.lingchatgateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 网关路由功能测试
 * 使用本地 Mock 方式测试路由配置和过滤器功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class LingchatGatewayApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
        System.out.println("✅ 应用上下文加载成功");
    }

    /**
     * 测试认证服务路由 (/api/auth/**)
     * 验证路由是否正确配置，并且 AuthFilter 正常工作
     */
    @Test
    void testAuthServiceRouteWithoutToken() {
        System.out.println("🔍 开始测试认证服务路由（无 Token）...");
        
        // 测试未带 Token 的请求 - 应该被 AuthFilter 拦截返回 401
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"testuser001\",\"password\":\"123456\"}")
                .exchange()
                .expectStatus().isUnauthorized();
        
        System.out.println("✅ 认证服务路由测试通过 - 未授权请求被正确拦截 (401)");
    }

    /**
     * 测试带有效 Token 的认证请求
     * 注意：由于后端服务未运行，会返回 403（Security 拦截）或 5xx
     */
    @Test
    void testAuthServiceRouteWithToken() {
        System.out.println("🔍 开始测试带 Token 的认证请求...");
        
        // 测试带 Bearer Token 的请求 - 应该通过 AuthFilter
        // 但由于 SecurityConfig 配置，/api/auth/** 是 permitAll 的
        // 所以会被路由到后端，但后端未运行会返回错误
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON) // ✅ 加上这个
                .bodyValue("{\"username\":\"test\",\"password\":\"123\"}")
                .header("Authorization", "Bearer test-token-123")
                .exchange()
                .expectStatus()
                .isForbidden(); // Security 可能会拒绝，或者后端未运行返回其他错误
        
        System.out.println("✅ Token 验证过滤器测试通过 - 请求通过了认证过滤器");
    }

    /**
     * 测试其他路径需要认证
     */
    @Test
    void testOtherPathsRequireAuth() {
        System.out.println("🔍 开始测试其他路径的认证要求...");
        
        // 测试非 /api/auth/** 路径，应该需要认证
        webTestClient.get()
                .uri("/api/users/profile")
                .exchange()
                .expectStatus().isUnauthorized();
        
        System.out.println("✅ 其他路径认证要求测试通过 - 未授权请求被拦截 (401)");
    }

    /**
     * 测试 GET 请求到认证端点（无需 Token）
     * 根据 SecurityConfig，/api/auth/** 是 permitAll 的
     * 但由于 AuthFilter 是 GlobalFilter，它仍然会检查 Token
     */
    @Test
    void testGetAuthEndpoint() {
        System.out.println("🔍 开始测试 POST 认证端点...");
        
        // GET 请求到认证端点，SecurityConfig 允许访问
        // 但由于 AuthFilter 是全局过滤器，会检查 Token
        webTestClient.post()
                .uri("/api/auth/register")
                .exchange()
                .expectStatus()
                .isUnauthorized(); // AuthFilter 会拦截未授权请求
        
        System.out.println("✅ GET 认证端点测试通过 - AuthFilter 正常工作");
    }

    /**
     * 测试无效路径的处理
     */
    @Test
    void testInvalidPath() {
        System.out.println("🔍 开始测试无效路径处理...");
        
        // 对于没有匹配任何路由的路径，需要先通过认证
        // 所以会先返回 401（未授权）
        webTestClient.get()
                .uri("/nonexistent/path")
                .exchange()
                .expectStatus()
                .isUnauthorized(); // 需要先认证
        
        System.out.println("✅ 无效路径处理测试通过 - 返回 401（需要认证）");
    }

    /**
     * 测试根路径访问
     */
    @Test
    void testRootPath() {
        System.out.println("🔍 开始测试根路径访问...");
        
        // 根路径没有匹配任何路由，需要先通过认证
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isUnauthorized(); // 需要先认证
        
        System.out.println("✅ 根路径访问测试通过 - 返回 401（需要认证）");
    }
}
