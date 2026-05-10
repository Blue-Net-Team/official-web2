package com.bluenet.web.application.command.judge;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 判题配置聚合的命令对象集合。
 */
public class JudgeProblemConfigCommands {

    private JudgeProblemConfigCommands() {
    }

    /**
     * 新增或替换判题配置命令。
     */
    public record UpsertCommand(
            String generatorLanguage,
            String generatorSource,
            String primaryStandardLanguage,
            Integer benchmarkRepeatTimes,
            BigDecimal marginMultiplier,
            Integer minExtraMs,
            Integer roundToMs,
            List<StandardSolutionCommand> standardSolutions,
            List<TestcaseConfigCommand> testcases) {
    }

    /**
     * 标准解命令。
     */
    public record StandardSolutionCommand(
            String language,
            String source,
            Boolean primarySolution) {
    }

    /**
     * 测试用例配置命令。
     */
    public record TestcaseConfigCommand(
            Integer caseNo,
            String category,
            JsonNode generatorArgs,
            BigDecimal weight,
            Boolean hidden,
            Boolean sample,
            String description) {
    }

    /**
     * 确认语言资源限制命令。
     */
    public record ConfirmLanguageLimitCommand(
            Integer timeLimitMs,
            Integer memoryLimitKb,
            Integer outputLimitKb) {
    }
}
