package com.bluenet.web.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JudgeLanguageLimitMapper {
    /**
     * 判断题目指定语言是否存在已确认的正式判题资源限制。
     *
     * @param questionId
     *            算法题目主键。
     * @param language
     *            编程语言值。
     * @return 已确认限制数量。
     */
    int countConfirmedByQuestionIdAndLanguage(@Param("questionId") Long questionId, @Param("language") String language);

    /**
     * 新增或更新管理员确认的语言资源限制。
     *
     * @param questionId
     *            算法题目主键。
     * @param language
     *            编程语言值。
     * @param timeLimitMs
     *            时间限制，单位毫秒。
     * @param memoryLimitKb
     *            内存限制，单位 KB。
     * @param outputLimitKb
     *            输出限制，单位 KB。
     * @param sourceConfigId
     *            资源限制来源的判题配置主键。
     * @return 无返回值。
     */
    void upsertConfirmedLimit(
            @Param("questionId") Long questionId,
            @Param("language") String language,
            @Param("timeLimitMs") Integer timeLimitMs,
            @Param("memoryLimitKb") Integer memoryLimitKb,
            @Param("outputLimitKb") Integer outputLimitKb,
            @Param("sourceConfigId") Long sourceConfigId);
}
