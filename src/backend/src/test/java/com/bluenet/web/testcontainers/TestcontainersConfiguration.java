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

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:17-alpine");
    private static final DockerImageName MINIO_IMAGE = DockerImageName.parse("minio/minio:latest");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7-alpine");

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE).withDatabaseName("db_blue_net_test")
                .withUsername("test")
                .withPassword("test");
    }

    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(REDIS_IMAGE).withExposedPorts(6379);
    }

    @Bean
    public MinIOContainer minioContainer(DynamicPropertyRegistry registry) {
        MinIOContainer container = new MinIOContainer(MINIO_IMAGE).withUserName("testuser")
                .withPassword("testpassword");

        registry.add("minio.enabled", () -> "true");
        registry.add("minio.endpoint", container::getHost);
        registry.add("minio.port", () -> container.getMappedPort(9000));
        registry.add("minio.access-key", container::getUserName);
        registry.add("minio.secret-key", container::getPassword);
        registry.add("minio.use-ssl", () -> "false");

        return container;
    }
}
