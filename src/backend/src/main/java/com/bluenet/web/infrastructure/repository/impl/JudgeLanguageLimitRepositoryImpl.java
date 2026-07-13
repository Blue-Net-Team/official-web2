package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.JudgeLanguageLimit;
import com.bluenet.web.domain.repository.JudgeLanguageLimitRepository;
import com.bluenet.web.infrastructure.repository.mapper.JudgeLanguageLimitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 算法题语言资源限制仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class JudgeLanguageLimitRepositoryImpl implements JudgeLanguageLimitRepository {

    private final JudgeLanguageLimitMapper judgeLanguageLimitMapper;

    @Override
    public boolean existsConfirmedByQuestionIdAndLanguage(Long questionId, String language) {
        int count = judgeLanguageLimitMapper.countConfirmedByQuestionIdAndLanguage(questionId, language);
        return count > 0;
    }

    @Override
    public void upsertConfirmedLimit(JudgeLanguageLimit limit) {
        judgeLanguageLimitMapper.upsertConfirmedLimit(
                limit.getQuestionId(),
                limit.getLanguage(),
                limit.getTimeLimitMs(),
                limit.getMemoryLimitKb(),
                limit.getOutputLimitKb(),
                limit.getSourceConfigId());
    }
}
