package com.bluenet.web.infrastructure.config;

import com.bluenet.web.infrastructure.storage.ObjectStorage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
/**
 * 对象存储启动配置。
 * <p>
 * 应用启动时通过当前启用的 {@link ObjectStorage} 初始化 bucket，避免业务首次写入时才发现 bucket 不存在。
 * </p>
 */
@Configuration
public class ObjectStorageConfig {

    /**
     * 应用启动时初始化对象存储 bucket。
     *
     * @param objectStorage
     *            当前启用的对象存储适配器
     * @return CommandLineRunner
     */
    @Bean
    @ConditionalOnProperty(name = "storage.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(ObjectStorage.class)
    public CommandLineRunner initializeObjectStorage(ObjectStorage objectStorage) {
        return args -> {
            log.info("ObjectStorageConfig: initializing object storage, class={}", objectStorage.getClass().getName());
            objectStorage.ensureBucket();
        };
    }
}
