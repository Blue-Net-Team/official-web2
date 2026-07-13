package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.JudgeStandardSolution;
import com.bluenet.web.domain.repository.JudgeStandardSolutionRepository;
import com.bluenet.web.infrastructure.repository.converter.JudgeStandardSolutionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeStandardSolutionDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeStandardSolutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 算法题标准解仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class JudgeStandardSolutionRepositoryImpl implements JudgeStandardSolutionRepository {

    private final JudgeStandardSolutionMapper judgeStandardSolutionMapper;
    private final JudgeStandardSolutionRepositoryConverter judgeStandardSolutionRepositoryConverter;

    @Override
    public List<JudgeStandardSolution> findByConfigId(Long configId) {
        List<JudgeStandardSolutionDO> dataObjects = judgeStandardSolutionMapper.selectByConfigId(configId);
        return judgeStandardSolutionRepositoryConverter.toEntityList(dataObjects);
    }

    @Override
    public void deleteByConfigId(Long configId) {
        judgeStandardSolutionMapper.deleteByConfigId(configId);
    }

    @Override
    public void save(JudgeStandardSolution solution) {
        JudgeStandardSolutionDO dataObject = judgeStandardSolutionRepositoryConverter.toDataObject(solution);
        judgeStandardSolutionMapper.insert(dataObject);
    }
}
