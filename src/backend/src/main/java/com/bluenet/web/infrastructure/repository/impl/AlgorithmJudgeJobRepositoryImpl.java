package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.infrastructure.repository.converter.AlgorithmJudgeJobRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AlgorithmJudgeJobDO;
import com.bluenet.web.infrastructure.repository.mapper.AlgorithmJudgeJobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 算法评测任务仓库实现类
 * <p>
 * 实现算法评测任务数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class AlgorithmJudgeJobRepositoryImpl implements AlgorithmJudgeJobRepository {
    private final AlgorithmJudgeJobMapper algorithmJudgeJobMapper;
    private final AlgorithmJudgeJobRepositoryConverter converter;

    @Override
    public void save(AlgorithmJudgeJob job) {
        AlgorithmJudgeJobDO dataObject = converter.toDataObject(job);
        if (dataObject.getId() == null) {
            algorithmJudgeJobMapper.insert(dataObject);
            job.setId(dataObject.getId());
        } else {
            algorithmJudgeJobMapper.updateById(dataObject);
        }
    }

    @Override
    public Optional<AlgorithmJudgeJob> findById(Long id) {
        AlgorithmJudgeJobDO dataObject = algorithmJudgeJobMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }
}
