package com.bluenet.web.api.converter.judge;

import com.bluenet.web.api.dto.judge.JudgeProblemConfigDTO;
import com.bluenet.web.api.dto.judge.JudgeStandardSolutionDTO;
import com.bluenet.web.api.dto.judge.JudgeTestcaseConfigDTO;
import com.bluenet.web.application.result.judge.JudgeProblemConfigResult;
import com.bluenet.web.application.result.judge.JudgeStandardSolutionResult;
import com.bluenet.web.application.result.judge.JudgeTestcaseConfigResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 判题配置响应转换器
 * <p>
 * 负责将判题配置 Result 转换为接口 DTO
 * </p>
 */
@Component
public class JudgeProblemConfigResponseConverter {

    public JudgeProblemConfigDTO toDTO(JudgeProblemConfigResult result) {
        if (result == null) {
            return null;
        }
        return new JudgeProblemConfigDTO(
                result.id(),
                result.questionId(),
                result.generatorLanguage(),
                result.generatorObjectKey(),
                result.generatorSource(),
                result.manifestObjectKey(),
                result.primaryStandardLanguage(),
                result.status(),
                result.benchmarkRepeatTimes(),
                result.marginMultiplier(),
                result.minExtraMs(),
                result.roundToMs(),
                toStandardSolutionDTOs(result.standardSolutions()),
                toTestcaseConfigDTOs(result.testcases()));
    }

    private List<JudgeStandardSolutionDTO> toStandardSolutionDTOs(List<JudgeStandardSolutionResult> results) {
        if (results == null) {
            return null;
        }
        return results.stream()
                .map(this::toStandardSolutionDTO)
                .toList();
    }

    private JudgeStandardSolutionDTO toStandardSolutionDTO(JudgeStandardSolutionResult result) {
        return new JudgeStandardSolutionDTO(
                result.language(),
                result.objectKey(),
                result.source(),
                result.objectHash(),
                result.primarySolution(),
                result.benchmarkStatus(),
                result.p95TimeMs(),
                result.maxTimeMs(),
                result.peakMemoryKb(),
                result.suggestedTimeLimitMs(),
                result.benchmarkMessage());
    }

    private List<JudgeTestcaseConfigDTO> toTestcaseConfigDTOs(List<JudgeTestcaseConfigResult> results) {
        if (results == null) {
            return null;
        }
        return results.stream()
                .map(this::toTestcaseConfigDTO)
                .toList();
    }

    private JudgeTestcaseConfigDTO toTestcaseConfigDTO(JudgeTestcaseConfigResult result) {
        return new JudgeTestcaseConfigDTO(
                result.caseNo(),
                result.category(),
                result.generatorArgs(),
                result.weight(),
                result.hidden(),
                result.sample(),
                result.description());
    }
}
