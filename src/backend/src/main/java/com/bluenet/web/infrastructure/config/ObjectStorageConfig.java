package com.bluenet.web.infrastructure.config;

import com.bluenet.web.infrastructure.storage.ObjectStorage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "storage.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(ObjectStorage.class)
    public CommandLineRunner initializeObjectStorage(ObjectStorage objectStorage) {
        return args -> objectStorage.ensureBucket();
    }
}
