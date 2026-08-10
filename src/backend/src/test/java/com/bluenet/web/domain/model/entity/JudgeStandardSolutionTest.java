package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeStandardSolution 领域实体单元测试。
 */
@DisplayName("JudgeStandardSolution 领域实体测试")
class JudgeStandardSolutionTest {

    @Test
    @DisplayName("create: 应创建新的标准解")
    void create_shouldCreateStandardSolution() {
        JudgeStandardSolution solution = JudgeStandardSolution.create(
                1L,
                2L,
                "cpp",
                "solution.cpp",
                "hash-1",
                true,
                "PENDING");

        assertThat(solution.getId()).isNull();
        assertThat(solution.getConfigId()).isEqualTo(1L);
        assertThat(solution.getQuestionId()).isEqualTo(2L);
        assertThat(solution.getLanguage()).isEqualTo("cpp");
        assertThat(solution.getObjectKey()).isEqualTo("solution.cpp");
        assertThat(solution.getObjectHash()).isEqualTo("hash-1");
        assertThat(solution.getPrimarySolution()).isTrue();
        assertThat(solution.getBenchmarkStatus()).isEqualTo("PENDING");
        assertThat(solution.getP95TimeMs()).isNull();
        assertThat(solution.getMaxTimeMs()).isNull();
        assertThat(solution.getPeakMemoryKb()).isNull();
        assertThat(solution.getSuggestedTimeLimitMs()).isNull();
        assertThat(solution.getBenchmarkMessage()).isNull();
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        JudgeStandardSolution solution = JudgeStandardSolution.reconstruct(
                100L,
                1L,
                2L,
                "java",
                "solution.java",
                "hash-2",
                false,
                "COMPLETED",
                150,
                200,
                1024,
                250,
                "benchmark completed");

        assertThat(solution.getId()).isEqualTo(100L);
        assertThat(solution.getConfigId()).isEqualTo(1L);
        assertThat(solution.getQuestionId()).isEqualTo(2L);
        assertThat(solution.getLanguage()).isEqualTo("java");
        assertThat(solution.getObjectKey()).isEqualTo("solution.java");
        assertThat(solution.getObjectHash()).isEqualTo("hash-2");
        assertThat(solution.getPrimarySolution()).isFalse();
        assertThat(solution.getBenchmarkStatus()).isEqualTo("COMPLETED");
        assertThat(solution.getP95TimeMs()).isEqualTo(150);
        assertThat(solution.getMaxTimeMs()).isEqualTo(200);
        assertThat(solution.getPeakMemoryKb()).isEqualTo(1024);
        assertThat(solution.getSuggestedTimeLimitMs()).isEqualTo(250);
        assertThat(solution.getBenchmarkMessage()).isEqualTo("benchmark completed");
    }
}
