package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.algorithm_judge.AlgorithmRunRequestDTO;
import com.bluenet.web.api.dto.algorithm_judge.AlgorithmSubmitResponseDTO;
import com.bluenet.web.api.dto.algorithm_judge.JudgeJobPollingResponseDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;

public interface AlgorithmJudgeService {
    AlgorithmSubmitResponseDTO run(AlgorithmRunRequestDTO request);

    AlgorithmSubmitResponseDTO submit(CreateAnswerRequestDTO request);

    JudgeJobPollingResponseDTO getJob(Long jobId);
}
