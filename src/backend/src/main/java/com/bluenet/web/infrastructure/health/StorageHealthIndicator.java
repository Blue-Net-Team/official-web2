package com.bluenet.web.infrastructure.health;

import com.bluenet.web.infrastructure.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(ObjectStorage.class)
public class StorageHealthIndicator implements HealthIndicator {

    private final ObjectStorage objectStorage;

    @Override
    public Health health() {
        try {
            objectStorage.checkHealth();
            log.debug("Object storage health check passed: {}", objectStorage.providerName());
            return Health.up()
                    .withDetail("service", "object-storage")
                    .withDetail("provider", objectStorage.providerName())
                    .build();
        } catch (Exception e) {
            log.warn("Object storage health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service", "object-storage")
                    .withDetail("provider", objectStorage.providerName())
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
