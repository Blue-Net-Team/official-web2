package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.judge.ConfirmJudgeLanguageLimitRequestDTO;
import com.bluenet.web.api.dto.judge.UpsertJudgeProblemConfigRequestDTO;
import com.bluenet.web.infrastructure.judge.JudgeTestDataGenerationPublisher;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeStandardSolutionDO;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeTestcaseConfigDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeLanguageLimitMapper;
import com.bluenet.web.infrastructure.repository.mapper.JudgeProblemConfigMapper;
import com.bluenet.web.infrastructure.repository.mapper.JudgeStandardSolutionMapper;
import com.bluenet.web.infrastructure.repository.mapper.JudgeTestcaseConfigMapper;
import com.bluenet.web.infrastructure.storage.JudgeAssetStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JudgeProblemConfigAdminServiceImpl 单元测试。
 */
@DisplayName("JudgeProblemConfigAdminServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class JudgeProblemConfigAdminServiceImplTest {

    private static final Long QUESTION_ID = 10L;
    private static final Long CONFIG_ID = 100L;

    @Mock
    private JudgeAssetStorage judgeAssetStorage;
    @Mock
    private JudgeTestDataGenerationPublisher testDataGenerationPublisher;
    @Mock
    private JudgeProblemConfigMapper judgeProblemConfigMapper;
    @Mock
    private JudgeStandardSolutionMapper judgeStandardSolutionMapper;
    @Mock
    private JudgeTestcaseConfigMapper judgeTestcaseConfigMapper;
    @Mock
    private JudgeLanguageLimitMapper judgeLanguageLimitMapper;

    private ObjectMapper objectMapper;
    private JudgeProblemConfigAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new JudgeProblemConfigAdminServiceImpl(
                objectMapper,
                judgeAssetStorage,
                testDataGenerationPublisher,
                judgeProblemConfigMapper,
                judgeStandardSolutionMapper,
                judgeTestcaseConfigMapper,
                judgeLanguageLimitMapper);
    }

    @Test
    @DisplayName("保存配置：主标准解语言不在列表中应拒绝")
    void upsert_primaryLanguageNotInSolutions_shouldThrow() {
        UpsertJudgeProblemConfigRequestDTO request = createValidRequest();
        UpsertJudgeProblemConfigRequestDTO invalidRequest = new UpsertJudgeProblemConfigRequestDTO(
                request.generatorLanguage(),
                request.generatorSource(),
                "java",
                request.benchmarkRepeatTimes(),
                request.marginMultiplier(),
                request.minExtraMs(),
                request.roundToMs(),
                request.standardSolutions(),
                request.testcases());

        assertThatThrownBy(() -> service.upsert(QUESTION_ID, invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("主标准解语言必须存在于标准解列表中");

        verify(judgeAssetStorage, never()).put(any(), any(), any());
    }

    @Test
    @DisplayName("保存配置：应上传 generator 和标准解到 OSS 并持久化配置")
    void upsert_validRequest_shouldUploadAssetsAndPersist() {
        UpsertJudgeProblemConfigRequestDTO request = createValidRequest();
        when(judgeProblemConfigMapper.upsertCurrentConfig(any())).thenReturn(CONFIG_ID);
        when(judgeProblemConfigMapper.selectByQuestionId(QUESTION_ID)).thenReturn(createConfigDO());

        service.upsert(QUESTION_ID, request);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(judgeAssetStorage, times(2))
                .put(keyCaptor.capture(), contentCaptor.capture(), eq("text/plain; charset=utf-8"));
        assertThat(keyCaptor.getAllValues().get(0)).startsWith("questions/" + QUESTION_ID + "/current/generator/");

        verify(judgeStandardSolutionMapper).deleteByConfigId(CONFIG_ID);
        verify(judgeStandardSolutionMapper).insert(any(JudgeStandardSolutionDO.class));
        verify(judgeTestcaseConfigMapper).deleteByConfigId(CONFIG_ID);
        verify(judgeTestcaseConfigMapper).insertConfig(any(JudgeTestcaseConfigDO.class));
        verify(judgeProblemConfigMapper).updateManifest(eq(CONFIG_ID), any(), any());
    }

    @Test
    @DisplayName("保存配置：生成的 manifest 应包含题目 ID 和 benchmark 配置")
    void upsert_shouldGenerateManifestWithCorrectStructure() throws Exception {
        UpsertJudgeProblemConfigRequestDTO request = createValidRequest();
        when(judgeProblemConfigMapper.upsertCurrentConfig(any())).thenReturn(CONFIG_ID);
        when(judgeProblemConfigMapper.selectByQuestionId(QUESTION_ID)).thenReturn(createConfigDO());

        service.upsert(QUESTION_ID, request);

        ArgumentCaptor<String> manifestKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> manifestContentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(judgeAssetStorage).put(
                manifestKeyCaptor.capture(),
                manifestContentCaptor.capture(),
                eq("application/json; charset=utf-8"));

        JsonNode manifest = objectMapper.readTree(manifestContentCaptor.getValue());
        assertThat(manifest.path("questionId").asLong()).isEqualTo(QUESTION_ID);
        assertThat(manifest.path("configId").asLong()).isEqualTo(CONFIG_ID);
        assertThat(manifest.path("primaryStandardLanguage").asText()).isEqualTo("python");
        assertThat(manifest.path("benchmark").path("repeatTimes").asInt()).isEqualTo(5);
        assertThat(manifest.path("benchmark").path("marginMultiplier").asDouble()).isEqualTo(1.5);
        assertThat(manifest.path("generator").path("objectKey").asText()).isNotBlank();
        assertThat(manifest.path("generator").path("sha256").asText()).isNotBlank();
    }

    @Test
    @DisplayName("确认语言限制：配置不存在时应拒绝")
    void confirmLanguageLimit_configNotFound_shouldThrow() {
        when(judgeProblemConfigMapper.selectIdByQuestionId(QUESTION_ID)).thenReturn(null);

        ConfirmJudgeLanguageLimitRequestDTO request = new ConfirmJudgeLanguageLimitRequestDTO(1000, 256 * 1024, 1024);

        assertThatThrownBy(() -> service.confirmLanguageLimit(QUESTION_ID, "python", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("判题配置不存在");

        verify(judgeLanguageLimitMapper, never()).upsertConfirmedLimit(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("确认语言限制：应持久化限制并尝试标记配置为 READY")
    void confirmLanguageLimit_valid_shouldPersistAndMarkReady() {
        when(judgeProblemConfigMapper.selectIdByQuestionId(QUESTION_ID)).thenReturn(CONFIG_ID);

        ConfirmJudgeLanguageLimitRequestDTO request = new ConfirmJudgeLanguageLimitRequestDTO(1000, 256 * 1024, 1024);
        service.confirmLanguageLimit(QUESTION_ID, "python", request);

        verify(judgeLanguageLimitMapper).upsertConfirmedLimit(
                QUESTION_ID,
                "python",
                1000,
                256 * 1024,
                1024,
                CONFIG_ID);
        verify(judgeProblemConfigMapper).markReadyIfGenerated(CONFIG_ID);
    }

    @Test
    @DisplayName("请求生成测试数据：配置不存在时应拒绝")
    void requestGeneration_configNotFound_shouldThrow() {
        when(judgeProblemConfigMapper.selectIdByQuestionId(QUESTION_ID)).thenReturn(null);

        assertThatThrownBy(() -> service.requestGeneration(QUESTION_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("判题配置不存在");

        verify(testDataGenerationPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("请求生成测试数据：应标记为生成中并发布消息")
    void requestGeneration_valid_shouldMarkGeneratingAndPublish() {
        when(judgeProblemConfigMapper.selectIdByQuestionId(QUESTION_ID)).thenReturn(CONFIG_ID);

        service.requestGeneration(QUESTION_ID);

        verify(judgeProblemConfigMapper).markGenerating(CONFIG_ID);
        verify(testDataGenerationPublisher).publish(CONFIG_ID);
    }

    private UpsertJudgeProblemConfigRequestDTO createValidRequest() {
        return new UpsertJudgeProblemConfigRequestDTO(
                "python",
                "print('hello')",
                "python",
                5,
                new BigDecimal("1.5"),
                50,
                50,
                List.of(
                        new UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest("python", "print(input())",
                                true)),
                List.of(
                        new UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest(
                                1, "NORMAL", null, BigDecimal.ONE, true, false, "普通用例")));
    }

    private JudgeProblemConfigDO createConfigDO() {
        JudgeProblemConfigDO config = new JudgeProblemConfigDO();
        config.setId(CONFIG_ID);
        config.setQuestionId(QUESTION_ID);
        config.setGeneratorLanguage("python");
        config.setGeneratorObjectKey("questions/" + QUESTION_ID + "/current/generator/abc.py");
        config.setGeneratorObjectHash("abc");
        config.setPrimaryStandardLanguage("python");
        config.setStatus("DRAFT");
        config.setBenchmarkRepeatTimes(5);
        config.setMarginMultiplier(new BigDecimal("1.5"));
        config.setMinExtraMs(50);
        config.setRoundToMs(50);
        return config;
    }
}
