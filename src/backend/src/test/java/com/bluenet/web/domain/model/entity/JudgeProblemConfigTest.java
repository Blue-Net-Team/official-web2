package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeProblemConfig 领域实体单元测试。
 */
@DisplayName("JudgeProblemConfig 领域实体测试")
class JudgeProblemConfigTest {

    @Test
    @DisplayName("create: 应创建新的判题配置")
    void create_shouldCreateProblemConfig() {
        BigDecimal marginMultiplier = new BigDecimal("1.5");
        JudgeProblemConfig config = JudgeProblemConfig.create(
                1L,
                "python",
                "generator.py",
                "hash-1",
                "cpp",
                5,
                marginMultiplier,
                50,
                100);

        assertThat(config.getId()).isNull();
        assertThat(config.getQuestionId()).isEqualTo(1L);
        assertThat(config.getGeneratorLanguage()).isEqualTo("python");
        assertThat(config.getGeneratorObjectKey()).isEqualTo("generator.py");
        assertThat(config.getGeneratorObjectHash()).isEqualTo("hash-1");
        assertThat(config.getManifestObjectKey()).isNull();
        assertThat(config.getManifestObjectHash()).isNull();
        assertThat(config.getPrimaryStandardLanguage()).isEqualTo("cpp");
        assertThat(config.getStatus()).isNull();
        assertThat(config.getBenchmarkRepeatTimes()).isEqualTo(5);
        assertThat(config.getMarginMultiplier()).isEqualTo(marginMultiplier);
        assertThat(config.getMinExtraMs()).isEqualTo(50);
        assertThat(config.getRoundToMs()).isEqualTo(100);
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        BigDecimal marginMultiplier = new BigDecimal("2.0");
        JudgeProblemConfig config = JudgeProblemConfig.reconstruct(
                100L,
                2L,
                "cpp",
                "gen.cpp",
                "hash-2",
                "manifest.json",
                "manifest-hash",
                "java",
                "READY",
                10,
                marginMultiplier,
                100,
                200);

        assertThat(config.getId()).isEqualTo(100L);
        assertThat(config.getQuestionId()).isEqualTo(2L);
        assertThat(config.getGeneratorLanguage()).isEqualTo("cpp");
        assertThat(config.getGeneratorObjectKey()).isEqualTo("gen.cpp");
        assertThat(config.getGeneratorObjectHash()).isEqualTo("hash-2");
        assertThat(config.getManifestObjectKey()).isEqualTo("manifest.json");
        assertThat(config.getManifestObjectHash()).isEqualTo("manifest-hash");
        assertThat(config.getPrimaryStandardLanguage()).isEqualTo("java");
        assertThat(config.getStatus()).isEqualTo("READY");
        assertThat(config.getBenchmarkRepeatTimes()).isEqualTo(10);
        assertThat(config.getMarginMultiplier()).isEqualTo(marginMultiplier);
        assertThat(config.getMinExtraMs()).isEqualTo(100);
        assertThat(config.getRoundToMs()).isEqualTo(200);
    }
}
