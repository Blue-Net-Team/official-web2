package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AlgorithmJudgeJob 领域实体单元测试。
 */
@DisplayName("AlgorithmJudgeJob 领域实体测试")
class AlgorithmJudgeJobTest {

    @Test
    @DisplayName("create: 应创建处于 PENDING 状态的评测任务")
    void create_shouldCreatePendingJob() {
        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                1L,
                2L,
                3L,
                4L,
                ProgrammingLanguage.CPP,
                "int main() {}",
                AlgorithmTestcaseType.FORMAL,
                null);

        assertThat(job.getId()).isNull();
        assertThat(job.getAnswerId()).isEqualTo(1L);
        assertThat(job.getQuestionId()).isEqualTo(2L);
        assertThat(job.getAssessmentTimeId()).isEqualTo(3L);
        assertThat(job.getUserId()).isEqualTo(4L);
        assertThat(job.getLanguage()).isEqualTo(ProgrammingLanguage.CPP);
        assertThat(job.getSourceCode()).isEqualTo("int main() {}");
        assertThat(job.getTestcaseType()).isEqualTo(AlgorithmTestcaseType.FORMAL);
        assertThat(job.getCustomInput()).isNull();
        assertThat(job.getStatus()).isEqualTo(JudgeJobStatus.PENDING);
        assertThat(job.getRetryCount()).isZero();
        assertThat(job.getMaxRetryCount()).isEqualTo(3);
        assertThat(job.getStatusMessage()).isEqualTo("等待判题");
        assertThat(job.getStartedAt()).isNull();
        assertThat(job.getFinishedAt()).isNull();
    }

    @Test
    @DisplayName("create: 编程语言为空应抛异常")
    void create_withNullLanguage_shouldThrow() {
        assertThatThrownBy(
                () -> AlgorithmJudgeJob.create(
                        1L,
                        2L,
                        3L,
                        4L,
                        null,
                        "code",
                        AlgorithmTestcaseType.FORMAL,
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("编程语言");
    }

    @Test
    @DisplayName("create: 源代码为空应抛异常")
    void create_withBlankSourceCode_shouldThrow() {
        assertThatThrownBy(
                () -> AlgorithmJudgeJob.create(
                        1L,
                        2L,
                        3L,
                        4L,
                        ProgrammingLanguage.JAVA,
                        "   ",
                        AlgorithmTestcaseType.FORMAL,
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("源代码");
    }

    @Test
    @DisplayName("markRunning: 应标记为 RUNNING 并设置开始时间")
    void markRunning_shouldSetRunningState() {
        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                1L,
                2L,
                3L,
                4L,
                ProgrammingLanguage.PYTHON,
                "print(1)",
                AlgorithmTestcaseType.DEFAULT_RUN,
                null);

        LocalDateTime before = LocalDateTime.now();
        job.markRunning();
        LocalDateTime after = LocalDateTime.now();

        assertThat(job.getStatus()).isEqualTo(JudgeJobStatus.RUNNING);
        assertThat(job.getStartedAt()).isAfterOrEqualTo(before);
        assertThat(job.getStartedAt()).isBeforeOrEqualTo(after);
        assertThat(job.getStatusMessage()).isEqualTo("正在判题");
    }

    @Test
    @DisplayName("markSucceeded: 应标记为 SUCCEEDED 并设置完成时间")
    void markSucceeded_shouldSetSucceededState() {
        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                1L,
                2L,
                3L,
                4L,
                ProgrammingLanguage.CPP,
                "code",
                AlgorithmTestcaseType.FORMAL,
                null);
        job.markRunning();

        LocalDateTime before = LocalDateTime.now();
        job.markSucceeded();
        LocalDateTime after = LocalDateTime.now();

        assertThat(job.getStatus()).isEqualTo(JudgeJobStatus.SUCCEEDED);
        assertThat(job.getFinishedAt()).isAfterOrEqualTo(before);
        assertThat(job.getFinishedAt()).isBeforeOrEqualTo(after);
        assertThat(job.getStatusMessage()).isEqualTo("判题完成");
    }

    @Test
    @DisplayName("markRetryableOrReview: 未达最大重试次数应标记为 RETRYING")
    void markRetryableOrReview_beforeMaxRetry_shouldSetRetrying() {
        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                1L,
                2L,
                3L,
                4L,
                ProgrammingLanguage.CPP,
                "code",
                AlgorithmTestcaseType.FORMAL,
                null);

        job.markRetryableOrReview("临时错误");

        assertThat(job.getRetryCount()).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(JudgeJobStatus.RETRYING);
        assertThat(job.getStatusMessage()).isEqualTo("临时错误");
    }

    @Test
    @DisplayName("markRetryableOrReview: 达到最大重试次数应标记为 FAILED_REVIEW_REQUIRED")
    void markRetryableOrReview_atMaxRetry_shouldSetReviewRequired() {
        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                1L,
                2L,
                3L,
                4L,
                ProgrammingLanguage.CPP,
                "code",
                AlgorithmTestcaseType.FORMAL,
                null);
        job.markRetryableOrReview("第一次失败");
        job.markRetryableOrReview("第二次失败");
        job.markRetryableOrReview("第三次失败");

        assertThat(job.getRetryCount()).isEqualTo(3);
        assertThat(job.getStatus()).isEqualTo(JudgeJobStatus.FAILED_REVIEW_REQUIRED);
    }

    @Test
    @DisplayName("markReviewRequired: 应直接标记为 FAILED_REVIEW_REQUIRED")
    void markReviewRequired_shouldSetReviewRequired() {
        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                1L,
                2L,
                3L,
                4L,
                ProgrammingLanguage.CPP,
                "code",
                AlgorithmTestcaseType.FORMAL,
                null);

        job.markReviewRequired("需要人工复核");

        assertThat(job.getStatus()).isEqualTo(JudgeJobStatus.FAILED_REVIEW_REQUIRED);
        assertThat(job.getStatusMessage()).isEqualTo("需要人工复核");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        LocalDateTime startedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2024, 1, 1, 10, 5);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 1, 10, 5);

        AlgorithmJudgeJob job = AlgorithmJudgeJob.reconstruct(
                100L,
                1L,
                2L,
                3L,
                4L,
                ProgrammingLanguage.JAVA,
                "source",
                AlgorithmTestcaseType.CUSTOM_RUN,
                "custom input",
                JudgeJobStatus.SUCCEEDED,
                1,
                5,
                "完成",
                startedAt,
                finishedAt,
                createdAt,
                updatedAt);

        assertThat(job.getId()).isEqualTo(100L);
        assertThat(job.getAnswerId()).isEqualTo(1L);
        assertThat(job.getQuestionId()).isEqualTo(2L);
        assertThat(job.getAssessmentTimeId()).isEqualTo(3L);
        assertThat(job.getUserId()).isEqualTo(4L);
        assertThat(job.getLanguage()).isEqualTo(ProgrammingLanguage.JAVA);
        assertThat(job.getSourceCode()).isEqualTo("source");
        assertThat(job.getTestcaseType()).isEqualTo(AlgorithmTestcaseType.CUSTOM_RUN);
        assertThat(job.getCustomInput()).isEqualTo("custom input");
        assertThat(job.getStatus()).isEqualTo(JudgeJobStatus.SUCCEEDED);
        assertThat(job.getRetryCount()).isEqualTo(1);
        assertThat(job.getMaxRetryCount()).isEqualTo(5);
        assertThat(job.getStatusMessage()).isEqualTo("完成");
        assertThat(job.getStartedAt()).isEqualTo(startedAt);
        assertThat(job.getFinishedAt()).isEqualTo(finishedAt);
        assertThat(job.getCreatedAt()).isEqualTo(createdAt);
        assertThat(job.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
