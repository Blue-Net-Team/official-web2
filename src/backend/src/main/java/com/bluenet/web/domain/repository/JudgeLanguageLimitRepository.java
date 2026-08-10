package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.JudgeLanguageLimit;

/**
 * 算法题语言资源限制仓储接口。
 */
public interface JudgeLanguageLimitRepository {
    /**
     * 判断题目指定语言是否存在已确认的正式判题资源限制。
     *
     * @param questionId
     *            算法题目主键。
     * @param language
     *            编程语言值。
     * @return 存在已确认限制时返回 true，否则返回 false。
     */
    boolean existsConfirmedByQuestionIdAndLanguage(Long questionId, String language);

    /**
     * 新增或更新管理员确认的语言资源限制。
     *
     * @param limit
     *            语言资源限制领域实体。
     */
    void upsertConfirmedLimit(JudgeLanguageLimit limit);
}
