package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeLanguageLimit 领域实体单元测试。
 */
@DisplayName("JudgeLanguageLimit 领域实体测试")
class JudgeLanguageLimitTest {

    @Test
    @DisplayName("createConfirmed: 应创建已确认的语言限制")
    void createConfirmed_shouldCreateConfirmedLimit() {
        LocalDateTime before = LocalDateTime.now();
        JudgeLanguageLimit limit = JudgeLanguageLimit.createConfirmed(
                1L,
                "cpp",
                1000,
                65536,
                1024,
                10L);
        LocalDateTime after = LocalDateTime.now();

        assertThat(limit.getId()).isNull();
        assertThat(limit.getQuestionId()).isEqualTo(1L);
        assertThat(limit.getLanguage()).isEqualTo("cpp");
        assertThat(limit.getTimeLimitMs()).isEqualTo(1000);
        assertThat(limit.getMemoryLimitKb()).isEqualTo(65536);
        assertThat(limit.getOutputLimitKb()).isEqualTo(1024);
        assertThat(limit.getConfirmed()).isTrue();
        assertThat(limit.getConfirmedAt()).isAfterOrEqualTo(before);
        assertThat(limit.getConfirmedAt()).isBeforeOrEqualTo(after);
        assertThat(limit.getSourceConfigId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        LocalDateTime confirmedAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        JudgeLanguageLimit limit = JudgeLanguageLimit.reconstruct(
                100L,
                2L,
                "java",
                2000,
                131072,
                2048,
                false,
                confirmedAt,
                20L);

        assertThat(limit.getId()).isEqualTo(100L);
        assertThat(limit.getQuestionId()).isEqualTo(2L);
        assertThat(limit.getLanguage()).isEqualTo("java");
        assertThat(limit.getTimeLimitMs()).isEqualTo(2000);
        assertThat(limit.getMemoryLimitKb()).isEqualTo(131072);
        assertThat(limit.getOutputLimitKb()).isEqualTo(2048);
        assertThat(limit.getConfirmed()).isFalse();
        assertThat(limit.getConfirmedAt()).isEqualTo(confirmedAt);
        assertThat(limit.getSourceConfigId()).isEqualTo(20L);
    }
}
