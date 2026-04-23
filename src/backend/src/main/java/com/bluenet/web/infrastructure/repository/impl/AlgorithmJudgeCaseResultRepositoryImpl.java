package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.infrastructure.repository.converter.AlgorithmJudgeCaseResultRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AlgorithmJudgeCaseResultDO;
import com.bluenet.web.infrastructure.repository.mapper.AlgorithmJudgeCaseResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 算法评测用例结果仓库实现类
 * <p>
 * 实现算法评测用例结果数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class AlgorithmJudgeCaseResultRepositoryImpl implements AlgorithmJudgeCaseResultRepository {
    private final AlgorithmJudgeCaseResultMapper algorithmJudgeCaseResultMapper;
    private final AlgorithmJudgeCaseResultRepositoryConverter converter;

    @Override
    public void saveAll(List<AlgorithmJudgeCaseResult> results) {
        for (AlgorithmJudgeCaseResult result : results) {
            AlgorithmJudgeCaseResultDO dataObject = converter.toDataObject(result);
            algorithmJudgeCaseResultMapper.insert(dataObject);
        }
    }

    @Override
    public List<AlgorithmJudgeCaseResult> findByJudgeJobId(Long judgeJobId) {
        List<AlgorithmJudgeCaseResultDO> dataObjects = algorithmJudgeCaseResultMapper.selectByJudgeJobId(judgeJobId);
        return converter.toEntityList(dataObjects);
    }
}
