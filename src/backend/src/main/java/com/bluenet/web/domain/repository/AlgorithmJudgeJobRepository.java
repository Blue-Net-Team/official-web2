package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.vo.AlgorithmJudgeJobVO;

import java.util.Optional;

public interface AlgorithmJudgeJobRepository {
    void save(AlgorithmJudgeJob job);

    Optional<AlgorithmJudgeJobVO> findById(Long id);

    void update(AlgorithmJudgeJobVO job);
}
