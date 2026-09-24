package com.bluenet.web.testcontainers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("pgvector/pgvector:pg17");
    // MinIO 已从 Docker Hub 下架且 quay.io 在 CI runner 上拒绝匿名拉取，
    // 测试改用 MinIO 平替 RustFS（Docker Hub，API 兼容 MinIO，支持 SigV4 预签名）。
    private static final DockerImageName RUSTFS_IMAGE = DockerImageName.parse("rustfs/rustfs:1.0.0");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7");

    private static final String S3_ACCESS_KEY = "testuser";
    private static final String S3_SECRET_KEY = "testpassword";

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
    public GenericContainer<?> s3Container(DynamicPropertyRegistry registry) {
        GenericContainer<?> container = new GenericContainer<>(RUSTFS_IMAGE)
                .withEnv("MINIO_ROOT_USER", S3_ACCESS_KEY)
                .withEnv("MINIO_ROOT_PASSWORD", S3_SECRET_KEY)
                .withExposedPorts(9000)
                .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000));

        registry.add("iven.storage.enabled", () -> "true");
        registry.add("iven.storage.provider", () -> "minio");
        registry.add("iven.storage.bucket", () -> "bluenet-test");
        registry.add("iven.storage.minio.endpoint", container::getHost);
        registry.add("iven.storage.minio.port", () -> container.getMappedPort(9000));
        registry.add("iven.storage.minio.accessKey", () -> S3_ACCESS_KEY);
        registry.add("iven.storage.minio.secretKey", () -> S3_SECRET_KEY);
        registry.add("iven.storage.minio.useSSL", () -> "false");

        return container;
    }
}
