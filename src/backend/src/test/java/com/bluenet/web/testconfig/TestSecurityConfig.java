package com.bluenet.web.testconfig;

import io.github.ivencn.infra.rbac.model.PermissionDefinition;
import io.github.ivencn.infra.rbac.spi.PermissionRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * 测试安全配置，禁用权限扫描同步（空实现，避免在测试中扫描权限）。
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public PermissionRegistry testPermissionRegistry() {
        return new PermissionRegistry() {
            @Override
            public void sync(List<PermissionDefinition> definitions) {
                // 测试中不执行权限持久化
            }
        };
    }
}
