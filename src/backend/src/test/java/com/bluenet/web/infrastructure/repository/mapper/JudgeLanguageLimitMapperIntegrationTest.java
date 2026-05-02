package com.bluenet.web.infrastructure.repository.mapper;

import com.bluenet.web.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeLanguageLimitMapper 集成测试。
 */
@DisplayName("JudgeLanguageLimitMapper 集成测试")
class JudgeLanguageLimitMapperIntegrationTest extends BaseIntegrationTest {

    private static final Long QUESTION_ID = 999L;
    private static final Long CONFIG_ID = 100L;

    @Autowired
    private JudgeLanguageLimitMapper judgeLanguageLimitMapper;
    @Autowired
    private JudgeProblemConfigMapper judgeProblemConfigMapper;

    @Test
    @DisplayName("确认语言限制：应插入记录并返回 count=1")
    void upsertConfirmedLimit_shouldInsertAndReturnCount() {
        // 需要先创建 config，因为 language_limit 有外键约束
        Long configId = createConfig();

        judgeLanguageLimitMapper.upsertConfirmedLimit(
                QUESTION_ID,
                "python",
                1000,
                256 * 1024,
                1024,
                configId);

        int count = judgeLanguageLimitMapper.countConfirmedByQuestionIdAndLanguage(QUESTION_ID, "python");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("更新已确认限制：应更新而非插入新记录")
    void upsertConfirmedLimit_existing_shouldUpdate() {
        Long configId = createConfig();
        judgeLanguageLimitMapper.upsertConfirmedLimit(
                QUESTION_ID,
                "python",
                1000,
                256 * 1024,
                1024,
                configId);

        judgeLanguageLimitMapper.upsertConfirmedLimit(
                QUESTION_ID,
                "python",
                2000,
                512 * 1024,
                2048,
                configId);

        int count = judgeLanguageLimitMapper.countConfirmedByQuestionIdAndLanguage(QUESTION_ID, "python");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("未确认的语言限制：count 应为 0")
    void countConfirmed_noRecord_shouldReturnZero() {
        int count = judgeLanguageLimitMapper.countConfirmedByQuestionIdAndLanguage(QUESTION_ID, "java");
        assertThat(count).isEqualTo(0);
    }

    private Long createConfig() {
        // 使用原生 SQL 插入 config，因为 upsertCurrentConfig 会返回自动生成的 id
        // 这里直接使用 mapper 的 upsert，但外键需要指向存在的 config id
        // 实际上 language_limit 的外键是 source_config_id -> tb_judge_problem_config(id)
        // 所以需要先插入 config
        com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO config = com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO
                .builder()
                .questionId(QUESTION_ID)
                .generatorLanguage("python")
                .generatorObjectKey("questions/999/current/generator/abc.py")
                .generatorObjectHash("abc")
                .primaryStandardLanguage("python")
                .benchmarkRepeatTimes(5)
                .marginMultiplier(new java.math.BigDecimal("1.5"))
                .minExtraMs(50)
                .roundToMs(50)
                .build();
        return judgeProblemConfigMapper.upsertCurrentConfig(config);
    }
}
