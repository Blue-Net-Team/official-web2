package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;
import com.bluenet.web.infrastructure.repository.dataobject.AlgorithmJudgeCaseResultDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 算法评测用例结果仓储转换器
 * <p>
 * 负责 AlgorithmJudgeCaseResult 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class AlgorithmJudgeCaseResultRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public AlgorithmJudgeCaseResultDO toDataObject(AlgorithmJudgeCaseResult entity) {
        if (entity == null) {
            return null;
        }
        return AlgorithmJudgeCaseResultDO.builder()
                .id(entity.getId())
                .judgeJobId(entity.getJudgeJobId())
                .caseNo(entity.getCaseNo())
                .testcaseType(entity.getTestcaseType())
                .status(entity.getStatus())
                .input(entity.getInput())
                .expectedOutput(entity.getExpectedOutput())
                .actualOutput(entity.getActualOutput())
                .stdout(entity.getStdout())
                .stderr(entity.getStderr())
                .timeUsedMs(entity.getTimeUsedMs())
                .memoryUsedKb(entity.getMemoryUsedKb())
                .message(entity.getMessage())
                .visibleToCandidate(entity.getVisibleToCandidate())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public AlgorithmJudgeCaseResult toEntity(AlgorithmJudgeCaseResultDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AlgorithmJudgeCaseResult.reconstruct(
                dataObject.getId(),
                dataObject.getJudgeJobId(),
                dataObject.getCaseNo(),
                dataObject.getTestcaseType(),
                dataObject.getStatus(),
                dataObject.getInput(),
                dataObject.getExpectedOutput(),
                dataObject.getActualOutput(),
                dataObject.getStdout(),
                dataObject.getStderr(),
                dataObject.getTimeUsedMs(),
                dataObject.getMemoryUsedKb(),
                dataObject.getMessage(),
                dataObject.getVisibleToCandidate(),
                dataObject.getCreatedAt());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<AlgorithmJudgeCaseResult> toEntityList(List<AlgorithmJudgeCaseResultDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
