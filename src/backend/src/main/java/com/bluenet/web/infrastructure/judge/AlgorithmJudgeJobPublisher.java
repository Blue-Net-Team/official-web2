package com.bluenet.web.infrastructure.judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;

public interface AlgorithmJudgeJobPublisher {
    void publish(Long judgeJobId, AlgorithmTestcaseType testcaseType);
}
