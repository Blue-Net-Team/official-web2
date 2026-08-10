package com.bluenet.web.application.service;

import com.bluenet.web.application.result.judge.JudgeProblemConfigResult;
import com.bluenet.web.application.command.judge.JudgeProblemConfigCommands;

import java.util.Optional;

public interface JudgeProblemConfigAdminService {
    /**
     * 新增或替换某道算法题的当前判题配置。
     *
     * @param questionId
     *            算法题目主键。
     * @param command
     *            管理员提交的判题配置命令。
     * @return 保存后的当前判题配置结果。
     */
    JudgeProblemConfigResult upsert(Long questionId, JudgeProblemConfigCommands.UpsertCommand command);

    /**
     * 查询某道算法题的当前判题配置。
     *
     * @param questionId
     *            算法题目主键。
     * @return 当前判题配置；不存在时返回空。
     */
    Optional<JudgeProblemConfigResult> findByQuestionId(Long questionId);

    /**
     * 请求 Judge Service 生成该题当前配置的测试数据。
     *
     * @param questionId
     *            算法题目主键。
     */
    void requestGeneration(Long questionId);

    /**
     * 确认某道算法题指定语言的正式判题资源限制。
     *
     * @param questionId
     *            算法题目主键。
     * @param language
     *            编程语言值。
     * @param command
     *            管理员确认的资源限制命令。
     */
    void confirmLanguageLimit(Long questionId, String language,
            JudgeProblemConfigCommands.ConfirmLanguageLimitCommand command);
}
