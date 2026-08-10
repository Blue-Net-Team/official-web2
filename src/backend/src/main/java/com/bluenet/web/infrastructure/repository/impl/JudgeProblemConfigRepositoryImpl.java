package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.domain.repository.JudgeProblemConfigRepository;
import com.bluenet.web.infrastructure.repository.converter.JudgeProblemConfigRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeProblemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 算法题判题配置仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class JudgeProblemConfigRepositoryImpl implements JudgeProblemConfigRepository {

    private final JudgeProblemConfigMapper judgeProblemConfigMapper;
    private final JudgeProblemConfigRepositoryConverter judgeProblemConfigRepositoryConverter;

    @Override
    public Long upsertCurrentConfig(JudgeProblemConfig config) {
        JudgeProblemConfigDO dataObject = judgeProblemConfigRepositoryConverter.toDataObject(config);
        return judgeProblemConfigMapper.upsertCurrentConfig(dataObject);
    }

    @Override
    public Optional<JudgeProblemConfig> findByQuestionId(Long questionId) {
        JudgeProblemConfigDO dataObject = judgeProblemConfigMapper.selectByQuestionId(questionId);
        JudgeProblemConfig entity = judgeProblemConfigRepositoryConverter.toEntity(dataObject);
        return Optional.ofNullable(entity);
    }

    @Override
    public Optional<Long> findIdByQuestionId(Long questionId) {
        Long configId = judgeProblemConfigMapper.selectIdByQuestionId(questionId);
        return Optional.ofNullable(configId);
    }

    @Override
    public void updateManifest(Long configId, String manifestObjectKey, String manifestObjectHash) {
        judgeProblemConfigMapper.updateManifest(configId, manifestObjectKey, manifestObjectHash);
    }

    @Override
    public void markGenerating(Long configId) {
        judgeProblemConfigMapper.markGenerating(configId);
    }

    @Override
    public void markReadyIfGenerated(Long configId) {
        judgeProblemConfigMapper.markReadyIfGenerated(configId);
    }

    @Override
    public void deleteByQuestionId(Long questionId) {
        judgeProblemConfigMapper.deleteByQuestionId(questionId);
    }
}
