package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.command.judge.JudgeProblemConfigCommands;
import com.bluenet.web.application.result.judge.JudgeProblemConfigResult;
import com.bluenet.web.domain.model.entity.JudgeLanguageLimit;
import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.domain.model.entity.JudgeStandardSolution;
import com.bluenet.web.domain.model.entity.JudgeTestcaseConfig;
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

import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    private static final String GENERATOR_SOURCE = "print('gen')";
    private static final String SOLUTION_SOURCE = "print('sol')";

    private static String sha256(String content) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private JudgeProblemConfigCommands.UpsertCommand command(String generatorSource) {
        return new JudgeProblemConfigCommands.UpsertCommand(
                "python",
                generatorSource,
                "python",
                5,
                new BigDecimal("1.2"),
                50,
                50,
                List.of(new JudgeProblemConfigCommands.StandardSolutionCommand("python", SOLUTION_SOURCE, true)),
                List.of(
                        new JudgeProblemConfigCommands.TestcaseConfigCommand(
                                1,
                                "SAMPLE",
                                new ObjectMapper().valueToTree(Map.of("n", 1)),
                                BigDecimal.ONE,
                                true,
                                false,
                                null)));
    }

    private JudgeProblemConfig storedConfig() {
        return JudgeProblemConfig.reconstruct(
                2L,
                1L,
                "python",
                "questions/1/current/generator/%s.py".formatted(sha256(GENERATOR_SOURCE)),
                sha256(GENERATOR_SOURCE),
                "manifest-key",
                "manifest-hash",
                "python",
                "READY",
                5,
                new BigDecimal("1.20"),
                50,
                50);
    }

    private void stubStoredChildren() {
        when(judgeStandardSolutionRepository.findByConfigId(2L))
                .thenReturn(
                        List.of(
                                JudgeStandardSolution.reconstruct(
                                        1L,
                                        2L,
                                        1L,
                                        "python",
                                        "solution-key",
                                        sha256(SOLUTION_SOURCE),
                                        true,
                                        "DONE",
                                        10,
                                        20,
                                        30,
                                        40,
                                        "ok")));
        when(judgeTestcaseConfigRepository.findByConfigId(2L))
                .thenReturn(
                        List.of(
                                JudgeTestcaseConfig.reconstruct(
                                        1L,
                                        2L,
                                        1,
                                        "SAMPLE",
                                        "{\"n\": 1}",
                                        new BigDecimal("1.00"),
                                        true,
                                        false,
                                        null)));
    }

    private void stubResultAssembly() {
        when(judgeProblemConfigRepository.findByQuestionId(1L)).thenReturn(Optional.of(storedConfig()));
        stubStoredChildren();
        when(judgeLanguageLimitRepository.findByQuestionId(1L)).thenReturn(List.of());
    }

    private JudgeProblemConfig capturedUpsertedConfig() {
        ArgumentCaptor<JudgeProblemConfig> captor = ArgumentCaptor.forClass(JudgeProblemConfig.class);
        verify(judgeProblemConfigRepository).upsertCurrentConfig(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("upsert: 判题内容未变时应沿用原状态并保留 benchmark 数据")
    void upsert_whenContentUnchanged_shouldKeepStatusAndBenchmark() {
        when(judgeProblemConfigRepository.upsertCurrentConfig(any())).thenReturn(2L);
        stubResultAssembly();

        service.upsert(1L, command(GENERATOR_SOURCE));

        assertThat(capturedUpsertedConfig().getStatus()).isEqualTo("READY");
        verify(judgeStandardSolutionRepository, never()).deleteByConfigId(anyLong());
        verify(judgeStandardSolutionRepository, never()).save(any());
        verify(judgeTestcaseConfigRepository, never()).deleteByConfigId(anyLong());
        verify(judgeTestcaseConfigRepository, never()).save(any());
    }

    @Test
    @DisplayName("upsert: generator 变更时应回退到草稿并重建子表")
    void upsert_whenGeneratorChanged_shouldResetToDraft() {
        when(judgeProblemConfigRepository.upsertCurrentConfig(any())).thenReturn(2L);
        stubResultAssembly();

        service.upsert(1L, command("print('another generator')"));

        assertThat(capturedUpsertedConfig().getStatus()).isEqualTo("DRAFT");
        verify(judgeStandardSolutionRepository).deleteByConfigId(2L);
        verify(judgeStandardSolutionRepository).save(any());
        verify(judgeTestcaseConfigRepository).deleteByConfigId(2L);
        verify(judgeTestcaseConfigRepository).save(any());
    }

    @Test
    @DisplayName("upsert: 首次保存应写入草稿状态")
    void upsert_whenConfigMissing_shouldInsertAsDraft() {
        when(judgeProblemConfigRepository.findByQuestionId(1L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(storedConfig()));
        when(judgeProblemConfigRepository.upsertCurrentConfig(any())).thenReturn(2L);
        stubStoredChildren();
        when(judgeLanguageLimitRepository.findByQuestionId(1L)).thenReturn(List.of());

        service.upsert(1L, command(GENERATOR_SOURCE));

        assertThat(capturedUpsertedConfig().getStatus()).isEqualTo("DRAFT");
        verify(judgeStandardSolutionRepository).save(any());
        verify(judgeTestcaseConfigRepository).save(any());
    }
}
