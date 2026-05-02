package com.bluenet.web.domain.repository;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AlgorithmJudgeJobRepository 集成测试。
 */
@DisplayName("AlgorithmJudgeJobRepository 集成测试")
class AlgorithmJudgeJobRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AlgorithmJudgeJobRepository algorithmJudgeJobRepository;

    @Test
    @DisplayName("保存并查询判题任务：应正确持久化所有字段")
    void saveAndFind_shouldPersistAndRetrieve() {
        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                null,
                10L,
                20L,
                1L,
                ProgrammingLanguage.PYTHON,
                "print(input())",
                AlgorithmTestcaseType.FORMAL,
                null);

        algorithmJudgeJobRepository.save(job);
        assertThat(job.getId()).isNotNull();

        Optional<AlgorithmJudgeJob> found = algorithmJudgeJobRepository.findById(job.getId());
        assertThat(found).isPresent();
        AlgorithmJudgeJob retrieved = found.get();
        assertThat(retrieved.getQuestionId()).isEqualTo(10L);
        assertThat(retrieved.getAssessmentTimeId()).isEqualTo(20L);
        assertThat(retrieved.getUserId()).isEqualTo(1L);
        assertThat(retrieved.getLanguage()).isEqualTo(ProgrammingLanguage.PYTHON);
        assertThat(retrieved.getSourceCode()).isEqualTo("print(input())");
        assertThat(retrieved.getTestcaseType()).isEqualTo(AlgorithmTestcaseType.FORMAL);
        assertThat(retrieved.getStatus()).isEqualTo(JudgeJobStatus.PENDING);
    }

    @Test
    @DisplayName("更新判题任务状态：应正确更新数据库")
    void update_shouldModifyStatus() {
        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                null,
                10L,
                20L,
                1L,
                ProgrammingLanguage.PYTHON,
                "print(1)",
                AlgorithmTestcaseType.DEFAULT_RUN,
                null);
        algorithmJudgeJobRepository.save(job);

        job.setStatus(JudgeJobStatus.SUCCEEDED);
        job.setStatusMessage("判题完成");
        algorithmJudgeJobRepository.update(job);

        Optional<AlgorithmJudgeJob> found = algorithmJudgeJobRepository.findById(job.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(JudgeJobStatus.SUCCEEDED);
        assertThat(found.get().getStatusMessage()).isEqualTo("判题完成");
    }
}
