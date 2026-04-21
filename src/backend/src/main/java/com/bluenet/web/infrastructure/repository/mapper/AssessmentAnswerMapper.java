package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentAnswerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssessmentAnswerMapper extends BaseMapper<AssessmentAnswerDO> {
    /**
     * 统计用户在指定考核场次中已提交的作答数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的记录数量。
     */
    int countByUserIdAndAssessmentTimeId(@Param("userId") Long userId,
            @Param("assessmentTimeId") Long assessmentTimeId);

    /**
     * 统计用户对指定题目的作答数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param questionId
     *            考核题目主键。
     * @return 满足条件的记录数量。
     */
    int countByUserIdAndQuestionId(@Param("userId") Long userId, @Param("questionId") Long questionId);

    /**
     * 按用户和题目查询作答数据行。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param questionId
     *            考核题目主键。
     * @return 匹配条件的考核作答 数据行；不存在时为 null。
     */
    AssessmentAnswerDO selectByUserIdAndQuestionId(@Param("userId") Long userId,
            @Param("questionId") Long questionId);

    /**
     * 按文件主键查询第一条关联作答数据行。
     *
     * @param fileId
     *            文件主键。
     * @return 匹配条件的考核作答 数据行；不存在时为 null。
     */
    AssessmentAnswerDO selectFirstByFileId(@Param("fileId") Long fileId);
}
