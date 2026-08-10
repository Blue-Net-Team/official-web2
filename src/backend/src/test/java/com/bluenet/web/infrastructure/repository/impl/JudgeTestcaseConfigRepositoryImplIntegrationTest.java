package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.domain.model.entity.JudgeTestcaseConfig;
import com.bluenet.web.domain.repository.JudgeProblemConfigRepository;
import com.bluenet.web.domain.repository.JudgeTestcaseConfigRepository;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeTestcaseConfigDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeTestcaseConfigMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeTestcaseConfigRepositoryImpl 集成测试。
 */
@DisplayName("JudgeTestcaseConfigRepositoryImpl 集成测试")
class JudgeTestcaseConfigRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JudgeTestcaseConfigRepository judgeTestcaseConfigRepository;

    @Autowired
    private JudgeTestcaseConfigMapper judgeTestcaseConfigMapper;

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

    private Long createTestcase(Long configId, Integer caseNo) {
        JudgeTestcaseConfig testcase = JudgeTestcaseConfig.create(
                configId,
                caseNo,
                "NORMAL",
                "{}",
                new BigDecimal("1.0"),
                true,
                false,
                "测试用例" + caseNo);
        judgeTestcaseConfigRepository.save(testcase);
        JudgeTestcaseConfigDO dataObject = judgeTestcaseConfigMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<JudgeTestcaseConfigDO>()
                        .eq("config_id", configId)
                        .eq("case_no", caseNo));
        return dataObject != null ? dataObject.getId() : null;
    }

    @Test
    @DisplayName("save: 新测试用例配置应插入")
    void save_newTestcase_shouldInsert() {
        Long configId = createConfig();
        Long testcaseId = createTestcase(configId, 1);

        assertThat(testcaseId).isNotNull();
        JudgeTestcaseConfigDO dataObject = judgeTestcaseConfigMapper.selectById(testcaseId);
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getCaseNo()).isEqualTo(1);
        assertThat(dataObject.getConfigId()).isEqualTo(configId);
    }

    @Test
    @DisplayName("findByConfigId: 应按配置ID查询所有测试用例配置")
    void findByConfigId_shouldReturnTestcases() {
        Long configId = createConfig();
        Long testcase1 = createTestcase(configId, 1);
        Long testcase2 = createTestcase(configId, 2);
        Long otherConfigId = createConfig();
        createTestcase(otherConfigId, 1);

        List<JudgeTestcaseConfig> testcases = judgeTestcaseConfigRepository.findByConfigId(configId);

        assertThat(testcases)
                .extracting(JudgeTestcaseConfig::getId)
                .containsExactlyInAnyOrder(testcase1, testcase2);
    }

    @Test
    @DisplayName("deleteByConfigId: 应删除指定配置下的所有测试用例配置")
    void deleteByConfigId_shouldRemoveTestcases() {
        Long configId = createConfig();
        Long testcaseId = createTestcase(configId, 1);

        judgeTestcaseConfigRepository.deleteByConfigId(configId);

        assertThat(judgeTestcaseConfigMapper.selectById(testcaseId)).isNull();
    }
}
