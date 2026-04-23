package com.bluenet.web.api.converter.algorithm_judge;

import com.bluenet.web.api.dto.algorithm_judge.AlgorithmRunRequestDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.application.command.algorithm_judge.AlgorithmJudgeCommands;
import org.springframework.stereotype.Component;

/**
 * 算法判题请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class AlgorithmJudgeRequestConverter {

    /**
     * 将运行请求 DTO 转换为命令
     */
    public AlgorithmJudgeCommands.RunCommand toCommand(AlgorithmRunRequestDTO dto) {
        return new AlgorithmJudgeCommands.RunCommand(
                dto.getQuestionId(),
                dto.getLanguage(),
                dto.getSourceCode(),
                dto.getTestcaseType(),
                dto.getCustomInput());
    }

    /**
     * 将提交请求 DTO 转换为命令
     */
    public AlgorithmJudgeCommands.SubmitCommand toCommand(CreateAnswerRequestDTO dto) {
        return new AlgorithmJudgeCommands.SubmitCommand(
                dto.getQuestionId(),
                dto.getLanguage(),
                dto.getContent());
    }
}
