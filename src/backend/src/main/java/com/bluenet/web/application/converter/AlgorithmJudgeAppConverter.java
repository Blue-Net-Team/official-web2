package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.algorithm_judge.AlgorithmSubmitResponseDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.algorithm_judge.JudgeCaseResultDTO;
import com.bluenet.web.api.dto.algorithm_judge.JudgeJobPollingResponseDTO;
import com.bluenet.web.application.AlgorithmJudgeResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 算法判题应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class AlgorithmJudgeAppConverter {

    /**
     * 将提交结果转换为 API 响应 DTO
     */
    public AlgorithmSubmitResponseDTO toSubmitDTO(AlgorithmJudgeResult.SubmitResult result) {
        return AlgorithmSubmitResponseDTO.builder()
                .judgeJobId(result.judgeJobId())
                .answerId(result.answerId())
                .testcaseType(result.testcaseType())
                .build();
    }

    /**
     * 将轮询结果转换为 API 响应 DTO
     */
    public JudgeJobPollingResponseDTO toPollingDTO(AlgorithmJudgeResult.PollResult result) {
        return JudgeJobPollingResponseDTO.builder()
                .judgeJobId(result.judgeJobId())
                .testcaseType(result.testcaseType())
                .status(result.status())
                .statusMessage(result.statusMessage())
                .caseResults(toCaseResultDTOList(result.caseResults()))
                .judgement(toJudgementDTO(result.judgement()))
                .build();
    }

    private List<JudgeCaseResultDTO> toCaseResultDTOList(List<AlgorithmJudgeResult.CaseResult> caseResults) {
        if (caseResults == null) {
            return List.of();
        }
        return caseResults.stream()
                .map(this::toCaseResultDTO)
                .toList();
    }

    private JudgeCaseResultDTO toCaseResultDTO(AlgorithmJudgeResult.CaseResult result) {
        return JudgeCaseResultDTO.builder()
                .caseNo(result.caseNo())
                .testcaseType(result.testcaseType())
                .status(result.status())
                .input(result.input())
                .expectedOutput(result.expectedOutput())
                .actualOutput(result.actualOutput())
                .stdout(result.stdout())
                .stderr(result.stderr())
                .timeUsedMs(result.timeUsedMs())
                .memoryUsedKb(result.memoryUsedKb())
                .message(result.message())
                .build();
    }

    private AssessmentJudgementDTO toJudgementDTO(AlgorithmJudgeResult.JudgementInfo judgement) {
        if (judgement == null) {
            return null;
        }
        return AssessmentJudgementDTO.builder()
                .id(judgement.id())
                .answerId(judgement.answerId())
                .questionId(judgement.questionId())
                .assessmentTimeId(judgement.assessmentTimeId())
                .userId(judgement.userId())
                .score(judgement.score())
                .maxScore(judgement.maxScore())
                .status(judgement.status())
                .resultCode(judgement.resultCode())
                .source(judgement.source())
                .reviewerId(judgement.reviewerId())
                .reviewerType(judgement.reviewerType())
                .comment(judgement.comment())
                .judgedAt(judgement.judgedAt())
                .build();
    }
}
