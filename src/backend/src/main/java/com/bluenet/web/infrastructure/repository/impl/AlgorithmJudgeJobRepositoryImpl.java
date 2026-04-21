package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.vo.AlgorithmJudgeJobVO;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.infrastructure.repository.mapper.AlgorithmJudgeJobMapper;
import com.bluenet.web.domain.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlgorithmJudgeJobRepositoryImpl implements AlgorithmJudgeJobRepository {
    private final AlgorithmJudgeJobMapper algorithmJudgeJobMapper;

    /**
     * 保存新的算法评测任务 记录。
     *
     * @param job
     *            算法评测任务领域对象。
     */
    @Override
    public void save(AlgorithmJudgeJob job) {
        RepositoryObjectConverter.insert(algorithmJudgeJobMapper, job, AlgorithmJudgeJobDO.class);
    }

    /**
     * 按主键查询算法评测任务 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的算法评测任务 结果；不存在时为空。
     */
    @Override
    public Optional<AlgorithmJudgeJobVO> findById(Long id) {
        AlgorithmJudgeJob job = RepositoryObjectConverter
                .toDomain(algorithmJudgeJobMapper.selectById(id), AlgorithmJudgeJob.class);
        if (job == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(job));
    }

    /**
     * 更新已有算法评测任务 记录。
     *
     * @param job
     *            算法评测任务领域对象。
     */
    @Override
    public void update(AlgorithmJudgeJobVO job) {
        AlgorithmJudgeJob entity = convertToEntity(job);
        int influence = RepositoryObjectConverter
                .updateById(algorithmJudgeJobMapper, entity, AlgorithmJudgeJobDO.class);
        if (influence == 0) {
            throw new GlobalException("更新算法判题任务失败");
        }
    }

    /**
     * 在算法评测任务 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param job
     *            算法评测任务领域对象。
     * @return 转换后的目标模型对象。
     */
    private AlgorithmJudgeJob convertToEntity(AlgorithmJudgeJobVO job) {
        AlgorithmJudgeJob entity = new AlgorithmJudgeJob();
        entity.setId(job.getId());
        entity.setAnswerId(job.getAnswerId());
        entity.setQuestionId(job.getQuestionId());
        entity.setAssessmentTimeId(job.getAssessmentTimeId());
        entity.setUserId(job.getUserId());
        entity.setLanguage(job.getLanguage());
        entity.setSourceCode(job.getSourceCode());
        entity.setTestcaseType(job.getTestcaseType());
        entity.setCustomInput(job.getCustomInput());
        entity.setStatus(job.getStatus());
        entity.setRetryCount(job.getRetryCount());
        entity.setMaxRetryCount(job.getMaxRetryCount());
        entity.setStatusMessage(job.getStatusMessage());
        entity.setStartedAt(job.getStartedAt());
        entity.setFinishedAt(job.getFinishedAt());
        entity.setCreatedAt(job.getCreatedAt());
        entity.setUpdatedAt(job.getUpdatedAt());
        return entity;
    }

    /**
     * 在算法评测任务 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param job
     *            算法评测任务领域对象。
     * @return 转换后的目标模型对象。
     */
    private AlgorithmJudgeJobVO convertToVO(AlgorithmJudgeJob job) {
        return AlgorithmJudgeJobVO.builder()
                .id(job.getId())
                .answerId(job.getAnswerId())
                .questionId(job.getQuestionId())
                .assessmentTimeId(job.getAssessmentTimeId())
                .userId(job.getUserId())
                .language(job.getLanguage())
                .sourceCode(job.getSourceCode())
                .testcaseType(job.getTestcaseType())
                .customInput(job.getCustomInput())
                .status(job.getStatus())
                .retryCount(job.getRetryCount())
                .maxRetryCount(job.getMaxRetryCount())
                .statusMessage(job.getStatusMessage())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
