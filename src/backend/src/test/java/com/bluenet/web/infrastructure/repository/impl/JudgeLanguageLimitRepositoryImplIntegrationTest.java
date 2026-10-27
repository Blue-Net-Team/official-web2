package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.DBIntegrationTest;
import com.bluenet.web.domain.model.entity.JudgeLanguageLimit;
import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.domain.repository.JudgeLanguageLimitRepository;
import com.bluenet.web.domain.repository.JudgeProblemConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeLanguageLimitRepositoryImpl 集成测试。
 */
@DisplayName("JudgeLanguageLimitRepositoryImpl 集成测试")
class JudgeLanguageLimitRepositoryImplIntegrationTest extends DBIntegrationTest {

    @Autowired
    private JudgeLanguageLimitRepository judgeLanguageLimitRepository;

    @Autowired
    private JudgeProblemConfigRepository judgeProblemConfigRepository;

    private final AtomicLong questionCounter = new AtomicLong(1000);

    private Long createConfig() {
        Long questionId = questionCounter.getAndIncrement();
        JudgeProblemConfig config = JudgeProblemConfig.create(
                questionId,
                "python",
                "generator.py",
                "hash-" + questionId,
                "cpp",
                5,
                new BigDecimal("1.5"),
                50,
                50);
        return judgeProblemConfigRepository.upsertCurrentConfig(config);
    }

    private void upsertLimit(Long questionId, Long sourceConfigId, String language, Integer timeLimitMs) {
        JudgeLanguageLimit limit = JudgeLanguageLimit.createConfirmed(
                questionId,
                language,
                timeLimitMs,
                65536,
                1024,
                sourceConfigId);
        judgeLanguageLimitRepository.upsertConfirmedLimit(limit);
    }

    @Test
    @DisplayName("upsertConfirmedLimit: 应插入已确认语言限制")
    void upsertConfirmedLimit_newLimit_shouldInsert() {
        Long configId = createConfig();
        Long questionId = configId + 100;
        upsertLimit(questionId, configId, "cpp", 1000);

        assertThat(judgeLanguageLimitRepository.existsConfirmedByQuestionIdAndLanguage(questionId, "cpp")).isTrue();
    }

    @Test
    @DisplayName("upsertConfirmedLimit: 同一题目和语言应更新限制")
    void upsertConfirmedLimit_sameQuestionAndLanguage_shouldUpdate() {
        Long configId = createConfig();
        Long questionId = configId + 100;
        upsertLimit(questionId, configId, "cpp", 1000);

        upsertLimit(questionId, configId, "cpp", 2000);

        assertThat(judgeLanguageLimitRepository.existsConfirmedByQuestionIdAndLanguage(questionId, "cpp")).isTrue();
    }

    @Test
    @DisplayName("findByQuestionId: 应返回该题全量限制及完整字段")
    void findByQuestionId_shouldReturnAllFields() {
        Long configId = createConfig();
        Long questionId = configId + 100;
        upsertLimit(questionId, configId, "python", 1200);

        List<JudgeLanguageLimit> limits = judgeLanguageLimitRepository.findByQuestionId(questionId);

        assertThat(limits).hasSize(1);
        JudgeLanguageLimit limit = limits.getFirst();
        assertThat(limit.getQuestionId()).isEqualTo(questionId);
        assertThat(limit.getLanguage()).isEqualTo("python");
        assertThat(limit.getTimeLimitMs()).isEqualTo(1200);
        assertThat(limit.getMemoryLimitKb()).isEqualTo(65536);
        assertThat(limit.getOutputLimitKb()).isEqualTo(1024);
        assertThat(limit.getConfirmed()).isTrue();
        assertThat(limit.getConfirmedAt()).isNotNull();
        assertThat(limit.getSourceConfigId()).isEqualTo(configId);
    }

    @Test
    @DisplayName("findByQuestionId: 多语言应按语言名升序返回")
    void findByQuestionId_multipleLanguages_shouldOrderByLanguage() {
        Long configId = createConfig();
        Long questionId = configId + 100;
        upsertLimit(questionId, configId, "python", 1200);
        upsertLimit(questionId, configId, "cpp", 1000);

        List<JudgeLanguageLimit> limits = judgeLanguageLimitRepository.findByQuestionId(questionId);

        assertThat(limits).extracting(JudgeLanguageLimit::getLanguage).containsExactly("cpp", "python");
    }

    @Test
    @DisplayName("findByQuestionId: 无限制行时应返回空列表")
    void findByQuestionId_withoutLimits_shouldReturnEmptyList() {
        assertThat(judgeLanguageLimitRepository.findByQuestionId(-1L)).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("existsConfirmedByQuestionIdAndLanguage: 应正确判断已确认限制是否存在")
    void existsConfirmedByQuestionIdAndLanguage_shouldWork() {
        Long configId = createConfig();
        Long questionId = configId + 100;
        upsertLimit(questionId, configId, "java", 1000);

        assertThat(judgeLanguageLimitRepository.existsConfirmedByQuestionIdAndLanguage(questionId, "java")).isTrue();
        assertThat(judgeLanguageLimitRepository.existsConfirmedByQuestionIdAndLanguage(questionId, "cpp")).isFalse();
        assertThat(judgeLanguageLimitRepository.existsConfirmedByQuestionIdAndLanguage(-1L, "java")).isFalse();
    }
}
