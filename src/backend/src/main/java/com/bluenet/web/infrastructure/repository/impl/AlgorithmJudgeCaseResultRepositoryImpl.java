package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;
import com.bluenet.web.domain.model.vo.AlgorithmJudgeCaseResultVO;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.infrastructure.repository.mapper.AlgorithmJudgeCaseResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AlgorithmJudgeCaseResultRepositoryImpl implements AlgorithmJudgeCaseResultRepository {
    private final AlgorithmJudgeCaseResultMapper algorithmJudgeCaseResultMapper;

    /**
     * 处理算法评测用例结果 仓储职责中的业务数据访问逻辑。
     *
     * @param results
     *            算法评测用例结果集合。
     */
    @Override
    public void saveAll(List<AlgorithmJudgeCaseResultVO> results) {
        for (AlgorithmJudgeCaseResultVO result : results) {
            RepositoryObjectConverter
                    .insert(algorithmJudgeCaseResultMapper, convertToEntity(result), AlgorithmJudgeCaseResultDO.class);
        }
    }

    /**
     * 查询符合条件的算法评测用例结果 记录。
     *
     * @param judgeJobId
     *            算法评测任务主键。
     * @return 满足条件的算法评测用例结果 结果集合。
     */
    @Override
    public List<AlgorithmJudgeCaseResultVO> findByJudgeJobId(Long judgeJobId) {
        return algorithmJudgeCaseResultMapper.selectByJudgeJobId(judgeJobId)
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * 在算法评测用例结果 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param result
     *            算法评测用例结果。
     * @return 转换后的目标模型对象。
     */
    private AlgorithmJudgeCaseResultVO convertToVO(AlgorithmJudgeCaseResultDO result) {
        return AlgorithmJudgeCaseResultVO.builder()
                .id(result.getId())
                .judgeJobId(result.getJudgeJobId())
                .caseNo(result.getCaseNo())
                .testcaseType(result.getTestcaseType())
                .status(result.getStatus())
                .input(result.getInput())
                .expectedOutput(result.getExpectedOutput())
                .actualOutput(result.getActualOutput())
                .stdout(result.getStdout())
                .stderr(result.getStderr())
                .timeUsedMs(result.getTimeUsedMs())
                .memoryUsedKb(result.getMemoryUsedKb())
                .message(result.getMessage())
                .visibleToCandidate(result.getVisibleToCandidate())
                .createdAt(result.getCreatedAt())
                .build();
    }

    /**
     * 在算法评测用例结果 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param result
     *            算法评测用例结果。
     * @return 转换后的目标模型对象。
     */
    private AlgorithmJudgeCaseResult convertToEntity(AlgorithmJudgeCaseResultVO result) {
        AlgorithmJudgeCaseResult entity = new AlgorithmJudgeCaseResult();
        entity.setId(result.getId());
        entity.setJudgeJobId(result.getJudgeJobId());
        entity.setCaseNo(result.getCaseNo());
        entity.setTestcaseType(result.getTestcaseType());
        entity.setStatus(result.getStatus());
        entity.setInput(result.getInput());
        entity.setExpectedOutput(result.getExpectedOutput());
        entity.setActualOutput(result.getActualOutput());
        entity.setStdout(result.getStdout());
        entity.setStderr(result.getStderr());
        entity.setTimeUsedMs(result.getTimeUsedMs());
        entity.setMemoryUsedKb(result.getMemoryUsedKb());
        entity.setMessage(result.getMessage());
        entity.setVisibleToCandidate(result.getVisibleToCandidate());
        entity.setCreatedAt(result.getCreatedAt());
        return entity;
    }
}
