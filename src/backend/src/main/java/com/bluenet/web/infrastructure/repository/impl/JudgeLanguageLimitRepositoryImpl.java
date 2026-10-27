package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.JudgeLanguageLimit;
import com.bluenet.web.domain.repository.JudgeLanguageLimitRepository;
import com.bluenet.web.infrastructure.repository.mapper.JudgeLanguageLimitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 算法题语言资源限制仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class JudgeLanguageLimitRepositoryImpl implements JudgeLanguageLimitRepository {

    private final JudgeLanguageLimitMapper judgeLanguageLimitMapper;

    @Override
    public List<JudgeLanguageLimit> findByQuestionId(Long questionId) {
        return judgeLanguageLimitMapper.selectByQuestionId(questionId)
                .stream()
                .map(
                        dataObject -> JudgeLanguageLimit.reconstruct(
                                dataObject.getId(),
                                dataObject.getQuestionId(),
                                dataObject.getLanguage(),
                                dataObject.getTimeLimitMs(),
                                dataObject.getMemoryLimitKb(),
                                dataObject.getOutputLimitKb(),
                                dataObject.getConfirmed(),
                                dataObject.getConfirmedAt(),
                                dataObject.getSourceConfigId()))
                .toList();
    }

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
