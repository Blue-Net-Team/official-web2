package com.bluenet.web.infrastructure.config.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SystemUserProperties 单元测试")
class SystemUserPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @EnableConfigurationProperties(SystemUserProperties.class)
    static class TestConfiguration {
    }

    @Test
    @DisplayName("默认配置值应正确绑定")
    void defaultValues_shouldBeCorrectlyBound() {
        contextRunner.run(context -> {
            SystemUserProperties properties = context.getBean(SystemUserProperties.class);
            assertThat(properties.getUsername()).isEqualTo("system");
            assertThat(properties.getPassword()).isEqualTo("admin123");
            assertThat(properties.getStudentId()).isEqualTo("000000000000");
        });
    }

    @Test
    @DisplayName("自定义配置值应正确绑定")
    void customValues_shouldBeCorrectlyBound() {
        contextRunner.withPropertyValues(
                "system-user.username=customAdmin",
                "system-user.password=customPassword123",
                "system-user.student-id=999999999999").run(context -> {
                    SystemUserProperties properties = context.getBean(SystemUserProperties.class);
                    assertThat(properties.getUsername()).isEqualTo("customAdmin");
                    assertThat(properties.getPassword()).isEqualTo("customPassword123");
                    assertThat(properties.getStudentId()).isEqualTo("999999999999");
                });
    }

    @Test
    @DisplayName("部分自定义配置值应与其他默认值共存")
    void partialCustomValues_shouldCoexistWithDefaults() {
        contextRunner.withPropertyValues("system-user.username=partialAdmin").run(context -> {
            SystemUserProperties properties = context.getBean(SystemUserProperties.class);
            assertThat(properties.getUsername()).isEqualTo("partialAdmin");
            assertThat(properties.getPassword()).isEqualTo("admin123");
            assertThat(properties.getStudentId()).isEqualTo("000000000000");
        });
    }
}
