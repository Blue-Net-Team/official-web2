package com.bluenet.web.application.command.algorithm_judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;

/**
 * 算法判题聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class AlgorithmJudgeCommands {

    /** 禁止实例化。 */
    private AlgorithmJudgeCommands() {
    }

    /**
     * 运行算法题命令。
     * <p>
     * 用于运行算法题目代码并查看结果。
     * </p>
     */
    public record RunCommand(
            /** 题目ID */
            Long questionId,
            /** 编程语言 */
            ProgrammingLanguage language,
            /** 源代码 */
            String sourceCode,
            /** 测试用例类型 */
            AlgorithmTestcaseType testcaseType,
            /** 自定义输入 */
            String customInput) {
    }

    /**
     * 提交算法题命令。
     * <p>
     * 用于提交算法题目代码进行评测。
     * </p>
     */
    public record SubmitCommand(
            /** 题目ID */
            Long questionId,
            /** 编程语言 */
            ProgrammingLanguage language,
            /** 内容 */
            String content) {
    }
}
