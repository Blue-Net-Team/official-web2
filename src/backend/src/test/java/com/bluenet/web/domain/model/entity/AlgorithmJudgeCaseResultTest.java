package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AlgorithmJudgeCaseResult 领域实体单元测试。
 */
@DisplayName("AlgorithmJudgeCaseResult 领域实体测试")
class AlgorithmJudgeCaseResultTest {

    @Test
    @DisplayName("create: 应创建新的评测用例结果")
    void create_shouldCreateCaseResult() {
        LocalDateTime before = LocalDateTime.now();
        AlgorithmJudgeCaseResult result = AlgorithmJudgeCaseResult.create(
                1L,
                1,
                AlgorithmTestcaseType.FORMAL,
                JudgeCaseStatus.AC,
                "input",
                "expected",
                "actual",
                "stdout",
                "stderr",
                100,
                1024,
                "通过",
                true);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result.getId()).isNull();
        assertThat(result.getJudgeJobId()).isEqualTo(1L);
        assertThat(result.getCaseNo()).isEqualTo(1);
        assertThat(result.getTestcaseType()).isEqualTo(AlgorithmTestcaseType.FORMAL);
        assertThat(result.getStatus()).isEqualTo(JudgeCaseStatus.AC);
        assertThat(result.getInput()).isEqualTo("input");
        assertThat(result.getExpectedOutput()).isEqualTo("expected");
        assertThat(result.getActualOutput()).isEqualTo("actual");
        assertThat(result.getStdout()).isEqualTo("stdout");
        assertThat(result.getStderr()).isEqualTo("stderr");
        assertThat(result.getTimeUsedMs()).isEqualTo(100);
        assertThat(result.getMemoryUsedKb()).isEqualTo(1024);
        assertThat(result.getMessage()).isEqualTo("通过");
        assertThat(result.getVisibleToCandidate()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(result.getCreatedAt()).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        AlgorithmJudgeCaseResult result = AlgorithmJudgeCaseResult.reconstruct(
                10L,
                2L,
                3,
                AlgorithmTestcaseType.CUSTOM_RUN,
                JudgeCaseStatus.TLE,
                "in",
                "exp",
                "act",
                "out",
                "err",
                2000,
                2048,
                "超时",
                false,
                createdAt);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getJudgeJobId()).isEqualTo(2L);
        assertThat(result.getCaseNo()).isEqualTo(3);
        assertThat(result.getTestcaseType()).isEqualTo(AlgorithmTestcaseType.CUSTOM_RUN);
        assertThat(result.getStatus()).isEqualTo(JudgeCaseStatus.TLE);
        assertThat(result.getInput()).isEqualTo("in");
        assertThat(result.getExpectedOutput()).isEqualTo("exp");
        assertThat(result.getActualOutput()).isEqualTo("act");
        assertThat(result.getStdout()).isEqualTo("out");
        assertThat(result.getStderr()).isEqualTo("err");
        assertThat(result.getTimeUsedMs()).isEqualTo(2000);
        assertThat(result.getMemoryUsedKb()).isEqualTo(2048);
        assertThat(result.getMessage()).isEqualTo("超时");
        assertThat(result.getVisibleToCandidate()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }
}
