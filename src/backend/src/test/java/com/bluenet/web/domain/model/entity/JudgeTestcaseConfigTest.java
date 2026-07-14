package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeTestcaseConfig 领域实体单元测试。
 */
@DisplayName("JudgeTestcaseConfig 领域实体测试")
class JudgeTestcaseConfigTest {

    @Test
    @DisplayName("create: 应创建新的测试用例配置")
    void create_shouldCreateTestcaseConfig() {
        BigDecimal weight = new BigDecimal("0.25");
        JudgeTestcaseConfig config = JudgeTestcaseConfig.create(
                1L,
                1,
                "formal",
                "{\"seed\":1}",
                weight,
                true,
                false,
                "正式用例1");

        assertThat(config.getId()).isNull();
        assertThat(config.getConfigId()).isEqualTo(1L);
        assertThat(config.getCaseNo()).isEqualTo(1);
        assertThat(config.getCategory()).isEqualTo("formal");
        assertThat(config.getGeneratorArgs()).isEqualTo("{\"seed\":1}");
        assertThat(config.getWeight()).isEqualTo(weight);
        assertThat(config.getHidden()).isTrue();
        assertThat(config.getSample()).isFalse();
        assertThat(config.getDescription()).isEqualTo("正式用例1");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        BigDecimal weight = new BigDecimal("0.5");
        JudgeTestcaseConfig config = JudgeTestcaseConfig.reconstruct(
                100L,
                2L,
                3,
                "sample",
                "{\"seed\":2}",
                weight,
                false,
                true,
                "样例用例");

        assertThat(config.getId()).isEqualTo(100L);
        assertThat(config.getConfigId()).isEqualTo(2L);
        assertThat(config.getCaseNo()).isEqualTo(3);
        assertThat(config.getCategory()).isEqualTo("sample");
        assertThat(config.getGeneratorArgs()).isEqualTo("{\"seed\":2}");
        assertThat(config.getWeight()).isEqualTo(weight);
        assertThat(config.getHidden()).isFalse();
        assertThat(config.getSample()).isTrue();
        assertThat(config.getDescription()).isEqualTo("样例用例");
    }
}
