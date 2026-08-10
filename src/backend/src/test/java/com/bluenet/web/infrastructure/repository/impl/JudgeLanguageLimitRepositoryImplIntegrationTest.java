package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.JudgeLanguageLimit;
import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.domain.repository.JudgeLanguageLimitRepository;
import com.bluenet.web.domain.repository.JudgeProblemConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeLanguageLimitRepositoryImpl 集成测试。
 */
@DisplayName("JudgeLanguageLimitRepositoryImpl 集成测试")
class JudgeLanguageLimitRepositoryImplIntegrationTest extends BaseIntegrationTest {

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
