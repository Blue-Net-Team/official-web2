package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.AlgorithmJudgeCaseResultVO;

import java.util.List;

public interface AlgorithmJudgeCaseResultRepository {
    void saveAll(List<AlgorithmJudgeCaseResultVO> results);

    List<AlgorithmJudgeCaseResultVO> findByJudgeJobId(Long judgeJobId);
}
