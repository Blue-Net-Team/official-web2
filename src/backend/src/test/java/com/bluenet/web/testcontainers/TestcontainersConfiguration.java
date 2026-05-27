package com.bluenet.web.testcontainers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("pgvector/pgvector:pg17");
    private static final DockerImageName MINIO_IMAGE = DockerImageName
            .parse("minio/minio:RELEASE.2025-09-07T16-13-09Z");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7");

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("db_blue_net_test")
                .withUsername("test")
                .withPassword("test");
    }

    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(REDIS_IMAGE)
                .withExposedPorts(6379);
    }

    @Bean
    public MinIOContainer minioContainer(DynamicPropertyRegistry registry) {
        MinIOContainer container = new MinIOContainer(MINIO_IMAGE)
                .withUserName("testuser")
                .withPassword("testpassword");

        registry.add("storage.enabled", () -> "true");
        registry.add("storage.provider", () -> "minio");
        registry.add("storage.bucket", () -> "bluenet-test");
        registry.add("storage.minio.endpoint", container::getHost);
        registry.add("storage.minio.port", () -> container.getMappedPort(9000));
        registry.add("storage.minio.accessKey", container::getUserName);
        registry.add("storage.minio.secretKey", container::getPassword);
        registry.add("storage.minio.useSSL", () -> "false");

        return container;
    }
}
