package com.bluenet.web.api.converter.judge;

import com.bluenet.web.api.dto.judge.ConfirmJudgeLanguageLimitRequestDTO;
import com.bluenet.web.api.dto.judge.UpsertJudgeProblemConfigRequestDTO;
import com.bluenet.web.application.command.judge.JudgeProblemConfigCommands;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 判题配置请求转换器
 * <p>
 * 负责将接口 DTO 转换为应用层 Command
 * </p>
 */
@Component
public class JudgeProblemConfigRequestConverter {

    public JudgeProblemConfigCommands.UpsertCommand toUpsertCommand(UpsertJudgeProblemConfigRequestDTO dto) {
        return new JudgeProblemConfigCommands.UpsertCommand(
                dto.generatorLanguage(),
                dto.generatorSource(),
                dto.primaryStandardLanguage(),
                dto.benchmarkRepeatTimes(),
                dto.marginMultiplier(),
                dto.minExtraMs(),
                dto.roundToMs(),
                toStandardSolutionCommands(dto.standardSolutions()),
                toTestcaseConfigCommands(dto.testcases()));
    }

    private List<JudgeProblemConfigCommands.StandardSolutionCommand> toStandardSolutionCommands(
            List<UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(
                        req -> new JudgeProblemConfigCommands.StandardSolutionCommand(
                                req.language(),
                                req.source(),
                                req.primarySolution()))
                .toList();
    }

    private List<JudgeProblemConfigCommands.TestcaseConfigCommand> toTestcaseConfigCommands(
            List<UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(
                        req -> new JudgeProblemConfigCommands.TestcaseConfigCommand(
                                req.caseNo(),
                                req.category(),
                                req.generatorArgs(),
                                req.weight(),
                                req.hidden(),
                                req.sample(),
                                req.description()))
                .toList();
    }

    public JudgeProblemConfigCommands.ConfirmLanguageLimitCommand toConfirmLanguageLimitCommand(
            ConfirmJudgeLanguageLimitRequestDTO dto) {
        return new JudgeProblemConfigCommands.ConfirmLanguageLimitCommand(
                dto.timeLimitMs(),
                dto.memoryLimitKb(),
                dto.outputLimitKb());
    }
}
