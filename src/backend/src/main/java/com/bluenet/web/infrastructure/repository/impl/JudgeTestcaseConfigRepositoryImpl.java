package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.JudgeTestcaseConfig;
import com.bluenet.web.domain.repository.JudgeTestcaseConfigRepository;
import com.bluenet.web.infrastructure.repository.converter.JudgeTestcaseConfigRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeTestcaseConfigDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeTestcaseConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 算法题测试用例生成配置仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class JudgeTestcaseConfigRepositoryImpl implements JudgeTestcaseConfigRepository {

    private final JudgeTestcaseConfigMapper judgeTestcaseConfigMapper;
    private final JudgeTestcaseConfigRepositoryConverter judgeTestcaseConfigRepositoryConverter;

    @Override
    public List<JudgeTestcaseConfig> findByConfigId(Long configId) {
        List<JudgeTestcaseConfigDO> dataObjects = judgeTestcaseConfigMapper.selectByConfigId(configId);
        return judgeTestcaseConfigRepositoryConverter.toEntityList(dataObjects);
    }

    @Override
    public void deleteByConfigId(Long configId) {
        judgeTestcaseConfigMapper.deleteByConfigId(configId);
    }

    @Override
    public void save(JudgeTestcaseConfig config) {
        JudgeTestcaseConfigDO dataObject = judgeTestcaseConfigRepositoryConverter.toDataObject(config);
        judgeTestcaseConfigMapper.insertConfig(dataObject);
    }
}
