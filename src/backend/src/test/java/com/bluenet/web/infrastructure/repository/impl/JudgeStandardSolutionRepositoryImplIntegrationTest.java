package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.domain.model.entity.JudgeStandardSolution;
import com.bluenet.web.domain.repository.JudgeProblemConfigRepository;
import com.bluenet.web.domain.repository.JudgeStandardSolutionRepository;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeStandardSolutionDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeStandardSolutionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeStandardSolutionRepositoryImpl 集成测试。
 */
@DisplayName("JudgeStandardSolutionRepositoryImpl 集成测试")
class JudgeStandardSolutionRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JudgeStandardSolutionRepository judgeStandardSolutionRepository;

    @Autowired
    private JudgeStandardSolutionMapper judgeStandardSolutionMapper;

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

    private Long createSolution(Long configId, Long questionId, String language) {
        JudgeStandardSolution solution = JudgeStandardSolution.create(
                configId,
                questionId,
                language,
                language + "/solution.cpp",
                "hash-" + language,
                true,
                "PENDING");
        judgeStandardSolutionRepository.save(solution);
        JudgeStandardSolutionDO dataObject = judgeStandardSolutionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<JudgeStandardSolutionDO>()
                        .eq("config_id", configId)
                        .eq("language", language));
        return dataObject != null ? dataObject.getId() : null;
    }

    @Test
    @DisplayName("save: 新标准解应插入")
    void save_newSolution_shouldInsert() {
        Long configId = createConfig();
        Long solutionId = createSolution(configId, configId + 100, "cpp");

        assertThat(solutionId).isNotNull();
        JudgeStandardSolutionDO dataObject = judgeStandardSolutionMapper.selectById(solutionId);
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getLanguage()).isEqualTo("cpp");
        assertThat(dataObject.getConfigId()).isEqualTo(configId);
    }

    @Test
    @DisplayName("findByConfigId: 应按配置ID查询所有标准解")
    void findByConfigId_shouldReturnSolutions() {
        Long configId = createConfig();
        Long solution1 = createSolution(configId, configId + 100, "cpp");
        Long solution2 = createSolution(configId, configId + 100, "java");
        Long otherConfigId = createConfig();
        createSolution(otherConfigId, otherConfigId + 100, "python");

        List<JudgeStandardSolution> solutions = judgeStandardSolutionRepository.findByConfigId(configId);

        assertThat(solutions)
                .extracting(JudgeStandardSolution::getId)
                .containsExactlyInAnyOrder(solution1, solution2);
    }

    @Test
    @DisplayName("deleteByConfigId: 应删除指定配置下的所有标准解")
    void deleteByConfigId_shouldRemoveSolutions() {
        Long configId = createConfig();
        Long solutionId = createSolution(configId, configId + 100, "cpp");

        judgeStandardSolutionRepository.deleteByConfigId(configId);

        assertThat(judgeStandardSolutionMapper.selectById(solutionId)).isNull();
    }
}
