package com.bluenet.judge.infrastructure.repository;

import com.bluenet.judge.infrastructure.repository.dataobject.AssessmentJudgementWrite;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeCaseResultWrite;
import com.bluenet.judge.infrastructure.repository.mapper.JudgeResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 判题结果持久化访问入口。
 */
@Repository
@RequiredArgsConstructor
public class JudgeResultRepository {
    /** 判题结果 MyBatis mapper。 */
    private final JudgeResultMapper judgeResultMapper;

    /**
     * 删除某个判题任务已有的用例结果。
     *
     * @param jobId
     *            判题任务主键。
     * @return 无返回值。
     */
    public void deleteCaseResults(Long jobId) {
        judgeResultMapper.deleteCaseResults(jobId);
    }

    /**
     * 写入单个判题用例结果。
     *
     * @param result
     *            判题用例结果写入对象。
     * @return 无返回值。
     */
    public void insertCaseResult(JudgeCaseResultWrite result) {
        // Formal judging keeps hidden successful cases invisible to candidates.
        judgeResultMapper.insertCaseResult(result);
    }

    /**
     * 写入正式提交的自动评判结果。
     *
     * @param judgement
     *            自动评判结果写入对象。
     * @return 无返回值。
     */
    public void insertAssessmentJudgement(AssessmentJudgementWrite judgement) {
        judgeResultMapper.insertAssessmentJudgement(judgement);
    }
}
