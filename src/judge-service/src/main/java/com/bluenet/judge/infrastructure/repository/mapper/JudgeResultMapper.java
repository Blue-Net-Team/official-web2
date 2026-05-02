package com.bluenet.judge.infrastructure.repository.mapper;

import com.bluenet.judge.infrastructure.repository.dataobject.AssessmentJudgementWrite;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeCaseResultWrite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JudgeResultMapper {
    /**
     * 删除某个判题任务已有的用例结果。
     *
     * @param jobId
     *            判题任务主键。
     * @return 无返回值。
     */
    void deleteCaseResults(@Param("jobId") Long jobId);

    /**
     * 写入单个判题用例结果。
     *
     * @param result
     *            判题用例结果写入对象。
     * @return 无返回值。
     */
    void insertCaseResult(@Param("result") JudgeCaseResultWrite result);

    /**
     * 写入自动评判结果。
     *
     * @param judgement
     *            自动评判结果写入对象。
     * @return 无返回值。
     */
    void insertAssessmentJudgement(@Param("judgement") AssessmentJudgementWrite judgement);
}
