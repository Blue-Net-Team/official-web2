package com.bluenet.web.testconfig;

import com.bluenet.web.infrastructure.security.scanner.PermissionScanner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 测试安全配置，禁用权限扫描器
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public PermissionScanner permissionScanner() {
        // 返回一个不执行任何操作的 PermissionScanner
        return new PermissionScanner(null, null, null) {
            @Override
            public void afterPropertiesSet() {
                // 什么都不做，避免在测试中扫描权限
            }
        };
    }
}
