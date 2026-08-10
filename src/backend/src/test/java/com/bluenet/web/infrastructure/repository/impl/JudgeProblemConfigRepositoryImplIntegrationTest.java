package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.domain.repository.JudgeProblemConfigRepository;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeProblemConfigMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeProblemConfigRepositoryImpl 集成测试。
 */
@DisplayName("JudgeProblemConfigRepositoryImpl 集成测试")
class JudgeProblemConfigRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JudgeProblemConfigRepository judgeProblemConfigRepository;

    @Autowired
    private JudgeProblemConfigMapper judgeProblemConfigMapper;

    private final AtomicLong questionCounter = new AtomicLong(1000);

    private Long createConfig(Long questionId) {
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

    private Long nextQuestionId() {
        return questionCounter.getAndIncrement();
    }

    @Test
    @DisplayName("upsertCurrentConfig: 新配置应插入并返回ID")
    void upsertCurrentConfig_newConfig_shouldInsertAndReturnId() {
        Long questionId = nextQuestionId();
        Long configId = createConfig(questionId);

        assertThat(configId).isNotNull();
        JudgeProblemConfigDO dataObject = judgeProblemConfigMapper.selectById(configId);
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getQuestionId()).isEqualTo(questionId);
        assertThat(dataObject.getGeneratorLanguage()).isEqualTo("python");
    }

    @Test
    @DisplayName("upsertCurrentConfig: 同一题目应更新现有配置")
    void upsertCurrentConfig_sameQuestion_shouldUpdate() {
        Long questionId = nextQuestionId();
        Long firstId = createConfig(questionId);
        JudgeProblemConfig config = JudgeProblemConfig.create(
                questionId,
                "cpp",
                "generator.cpp",
                "hash-updated",
                "java",
                10,
                new BigDecimal("2.0"),
                100,
                100);

        Long updatedId = judgeProblemConfigRepository.upsertCurrentConfig(config);

        assertThat(updatedId).isEqualTo(firstId);
        JudgeProblemConfigDO updated = judgeProblemConfigMapper.selectById(firstId);
        assertThat(updated.getGeneratorLanguage()).isEqualTo("cpp");
        assertThat(updated.getBenchmarkRepeatTimes()).isEqualTo(10);
    }

    @Test
    @DisplayName("findByQuestionId: 存在返回配置，不存在返回空")
    void findByQuestionId_shouldReturnOptional() {
        Long questionId = nextQuestionId();
        Long configId = createConfig(questionId);

        Optional<JudgeProblemConfig> found = judgeProblemConfigRepository.findByQuestionId(questionId);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(configId);

        assertThat(judgeProblemConfigRepository.findByQuestionId(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findIdByQuestionId: 应返回配置主键")
    void findIdByQuestionId_shouldReturnId() {
        Long questionId = nextQuestionId();
        Long configId = createConfig(questionId);

        Optional<Long> found = judgeProblemConfigRepository.findIdByQuestionId(questionId);
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(configId);

        assertThat(judgeProblemConfigRepository.findIdByQuestionId(-1L)).isEmpty();
    }

    @Test
    @DisplayName("updateManifest: 应更新 manifest 信息")
    void updateManifest_shouldUpdate() {
        Long questionId = nextQuestionId();
        Long configId = createConfig(questionId);

        judgeProblemConfigRepository.updateManifest(configId, "manifest-key", "manifest-hash");

        JudgeProblemConfigDO updated = judgeProblemConfigMapper.selectById(configId);
        assertThat(updated.getManifestObjectKey()).isEqualTo("manifest-key");
        assertThat(updated.getManifestObjectHash()).isEqualTo("manifest-hash");
    }

    @Test
    @DisplayName("markGenerating: 应将配置标记为生成中")
    void markGenerating_shouldUpdateStatus() {
        Long questionId = nextQuestionId();
        Long configId = createConfig(questionId);

        judgeProblemConfigRepository.markGenerating(configId);

        JudgeProblemConfigDO updated = judgeProblemConfigMapper.selectById(configId);
        assertThat(updated.getStatus()).isEqualTo("GENERATING");
    }

    @Test
    @DisplayName("deleteByQuestionId: 应删除配置")
    void deleteByQuestionId_shouldRemoveConfig() {
        Long questionId = nextQuestionId();
        Long configId = createConfig(questionId);

        judgeProblemConfigRepository.deleteByQuestionId(questionId);

        assertThat(judgeProblemConfigMapper.selectById(configId)).isNull();
    }
}
