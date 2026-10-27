package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.judge.JudgeProblemConfigResult;
import com.bluenet.web.domain.model.entity.JudgeLanguageLimit;
import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.domain.repository.JudgeLanguageLimitRepository;
import com.bluenet.web.domain.repository.JudgeProblemConfigRepository;
import com.bluenet.web.domain.repository.JudgeStandardSolutionRepository;
import com.bluenet.web.domain.repository.JudgeTestcaseConfigRepository;
import com.bluenet.web.infrastructure.judge.JudgeTestDataGenerationPublisher;
import com.bluenet.web.infrastructure.storage.JudgeAssetStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("JudgeProblemConfigAdminServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class JudgeProblemConfigAdminServiceImplTest {

    @Mock
    private JudgeAssetStorage judgeAssetStorage;
    @Mock
    private JudgeTestDataGenerationPublisher testDataGenerationPublisher;
    @Mock
    private JudgeProblemConfigRepository judgeProblemConfigRepository;
    @Mock
    private JudgeStandardSolutionRepository judgeStandardSolutionRepository;
    @Mock
    private JudgeTestcaseConfigRepository judgeTestcaseConfigRepository;
    @Mock
    private JudgeLanguageLimitRepository judgeLanguageLimitRepository;

    private JudgeProblemConfigAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JudgeProblemConfigAdminServiceImpl(
                new ObjectMapper(),
                judgeAssetStorage,
                testDataGenerationPublisher,
                judgeProblemConfigRepository,
                judgeStandardSolutionRepository,
                judgeTestcaseConfigRepository,
                judgeLanguageLimitRepository);
    }

    @Test
    @DisplayName("findByQuestionId: 应组装题目语言资源限制")
    void findByQuestionId_shouldIncludeLanguageLimits() {
        JudgeProblemConfig config = config();
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
        JudgeLanguageLimit limit = JudgeLanguageLimit.reconstruct(
                10L,
                1L,
                "python",
                1200,
                262144,
                1024,
                true,
                confirmedAt,
                2L);
        when(judgeProblemConfigRepository.findByQuestionId(1L)).thenReturn(Optional.of(config));
        when(judgeStandardSolutionRepository.findByConfigId(2L)).thenReturn(List.of());
        when(judgeTestcaseConfigRepository.findByConfigId(2L)).thenReturn(List.of());
        when(judgeLanguageLimitRepository.findByQuestionId(1L)).thenReturn(List.of(limit));

        Optional<JudgeProblemConfigResult> result = service.findByQuestionId(1L);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().languageLimits()).singleElement().satisfies(item -> {
            assertThat(item.language()).isEqualTo("python");
            assertThat(item.timeLimitMs()).isEqualTo(1200);
            assertThat(item.memoryLimitKb()).isEqualTo(262144);
            assertThat(item.outputLimitKb()).isEqualTo(1024);
            assertThat(item.confirmed()).isTrue();
            assertThat(item.confirmedAt()).isEqualTo(confirmedAt);
        });
    }

    @Test
    @DisplayName("findByQuestionId: 配置不存在时仍返回空 Optional")
    void findByQuestionId_whenConfigMissing_shouldReturnEmpty() {
        when(judgeProblemConfigRepository.findByQuestionId(1L)).thenReturn(Optional.empty());

        assertThat(service.findByQuestionId(1L)).isEmpty();
        verifyNoInteractions(judgeLanguageLimitRepository);
    }

    private JudgeProblemConfig config() {
        return JudgeProblemConfig.reconstruct(
                2L,
                1L,
                "python",
                null,
                "generator-hash",
                null,
                null,
                "python",
                "READY",
                5,
                BigDecimal.valueOf(1.2),
                50,
                50);
    }
}
