package com.bluenet.web.infrastructure.repository.impl;

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

    @Override
    public void save(AlgorithmJudgeJob job) {
        algorithmJudgeJobMapper.insert(job);
    }

    @Override
    public Optional<AlgorithmJudgeJobVO> findById(Long id) {
        AlgorithmJudgeJob job = algorithmJudgeJobMapper.selectById(id);
        if (job == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(job));
    }

    @Override
    public void update(AlgorithmJudgeJobVO job) {
        AlgorithmJudgeJob entity = convertToEntity(job);
        int influence = algorithmJudgeJobMapper.updateById(entity);
        if (influence == 0) {
            throw new GlobalException("更新算法判题任务失败");
        }
    }

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
