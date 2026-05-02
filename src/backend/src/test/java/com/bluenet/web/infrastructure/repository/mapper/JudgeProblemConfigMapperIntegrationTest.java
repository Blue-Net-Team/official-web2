package com.bluenet.web.infrastructure.repository.mapper;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeProblemConfigMapper 集成测试。
 */
@DisplayName("JudgeProblemConfigMapper 集成测试")
class JudgeProblemConfigMapperIntegrationTest extends BaseIntegrationTest {

    private static final Long QUESTION_ID = 999L;

    @Autowired
    private JudgeProblemConfigMapper judgeProblemConfigMapper;

    @Test
    @DisplayName("Upsert 配置：应插入新记录并返回主键")
    void upsertCurrentConfig_newConfig_shouldInsertAndReturnId() {
        JudgeProblemConfigDO config = createConfigDO();

        Long configId = judgeProblemConfigMapper.upsertCurrentConfig(config);

        assertThat(configId).isNotNull();
        JudgeProblemConfigDO found = judgeProblemConfigMapper.selectByQuestionId(QUESTION_ID);
        assertThat(found).isNotNull();
        assertThat(found.getQuestionId()).isEqualTo(QUESTION_ID);
        assertThat(found.getGeneratorLanguage()).isEqualTo("python");
        assertThat(found.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("Upsert 配置：同一题目应更新而非插入新记录")
    void upsertCurrentConfig_sameQuestion_shouldUpdate() {
        JudgeProblemConfigDO config1 = createConfigDO();
        config1.setGeneratorLanguage("python");
        Long id1 = judgeProblemConfigMapper.upsertCurrentConfig(config1);

        JudgeProblemConfigDO config2 = createConfigDO();
        config2.setGeneratorLanguage("java");
        Long id2 = judgeProblemConfigMapper.upsertCurrentConfig(config2);

        assertThat(id1).isEqualTo(id2);
        JudgeProblemConfigDO found = judgeProblemConfigMapper.selectByQuestionId(QUESTION_ID);
        assertThat(found.getGeneratorLanguage()).isEqualTo("java");
        assertThat(found.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("更新 manifest：应正确更新对象键和哈希")
    void updateManifest_shouldUpdateObjectKeyAndHash() {
        Long configId = judgeProblemConfigMapper.upsertCurrentConfig(createConfigDO());

        judgeProblemConfigMapper.updateManifest(configId, "questions/999/current/manifest/abc.json", "abc123");

        JudgeProblemConfigDO found = judgeProblemConfigMapper.selectByQuestionId(QUESTION_ID);
        assertThat(found.getManifestObjectKey()).isEqualTo("questions/999/current/manifest/abc.json");
        assertThat(found.getManifestObjectHash()).isEqualTo("abc123");
    }

    @Test
    @DisplayName("标记生成中：应将状态更新为 GENERATING")
    void markGenerating_shouldUpdateStatus() {
        Long configId = judgeProblemConfigMapper.upsertCurrentConfig(createConfigDO());

        judgeProblemConfigMapper.markGenerating(configId);

        JudgeProblemConfigDO found = judgeProblemConfigMapper.selectByQuestionId(QUESTION_ID);
        assertThat(found.getStatus()).isEqualTo("GENERATING");
    }

    @Test
    @DisplayName("标记 READY：仅在 GENERATED 状态时才更新")
    void markReadyIfGenerated_shouldOnlyUpdateWhenGenerated() {
        Long configId = judgeProblemConfigMapper.upsertCurrentConfig(createConfigDO());

        judgeProblemConfigMapper.markReadyIfGenerated(configId);

        JudgeProblemConfigDO found = judgeProblemConfigMapper.selectByQuestionId(QUESTION_ID);
        assertThat(found.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("删除配置：应按题目级联删除")
    void deleteByQuestionId_shouldRemoveConfig() {
        judgeProblemConfigMapper.upsertCurrentConfig(createConfigDO());

        judgeProblemConfigMapper.deleteByQuestionId(QUESTION_ID);

        assertThat(judgeProblemConfigMapper.selectByQuestionId(QUESTION_ID)).isNull();
    }

    private JudgeProblemConfigDO createConfigDO() {
        return JudgeProblemConfigDO.builder()
                .questionId(QUESTION_ID)
                .generatorLanguage("python")
                .generatorObjectKey("questions/999/current/generator/abc.py")
                .generatorObjectHash("abc")
                .primaryStandardLanguage("python")
                .benchmarkRepeatTimes(5)
                .marginMultiplier(new BigDecimal("1.5"))
                .minExtraMs(50)
                .roundToMs(50)
                .build();
    }
}
