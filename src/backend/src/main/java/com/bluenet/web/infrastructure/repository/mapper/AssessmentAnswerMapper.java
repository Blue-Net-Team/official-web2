package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentAnswerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 按队伍和题目查询作答数据行。
     *
     * @param teamId
     *            队伍主键。
     * @param questionId
     *            考核题目主键。
     * @return 匹配条件的考核作答 数据行列表。
     */
    List<AssessmentAnswerDO> selectByTeamIdAndQuestionId(@Param("teamId") Long teamId,
            @Param("questionId") Long questionId);

    /**
     * 按队伍主键删除所有作答数据行。
     *
     * @param teamId
     *            队伍主键。
     * @return 删除的记录数量。
     */
    int deleteByTeamId(@Param("teamId") Long teamId);

    /**
     * 统计指定队伍的作答数量。
     *
     * @param teamId
     *            队伍主键。
     * @return 满足条件的记录数量。
     */
    int countByTeamId(@Param("teamId") Long teamId);

    /**
     * 按队伍主键查询所有作答主键。
     *
     * @param teamId
     *            队伍主键。
     * @return 作答主键列表。
     */
    List<Long> selectAnswerIdsByTeamId(@Param("teamId") Long teamId);

    /**
     * 批量插入作答数据行。
     *
     * @param answers
     *            作答数据对象列表。
     * @return 插入的记录数量。
     */
    int batchInsert(@Param("answers") List<AssessmentAnswerDO> answers);

    /**
     * 批量更新组员作答（排除队长）。
     *
     * @param teamId
     *            队伍主键。
     * @param leaderId
     *            队长用户主键。
     * @param questionId
     *            考核题目主键。
     * @param fileId
     *            文件主键。
     * @param content
     *            答案内容。
     * @param language
     *            编程语言。
     * @param submitTime
     *            提交时间。
     * @return 更新的记录数量。
     */
    int updateTeamMemberAnswers(@Param("teamId") Long teamId,
            @Param("questionId") Long questionId, @Param("fileId") Long fileId,
            @Param("content") String content,
            @Param("language") com.bluenet.web.domain.model.enumerate.ProgrammingLanguage language,
            @Param("submitTime") java.time.LocalDateTime submitTime);

    /**
     * 批量查询已有答案的用户主键。
     *
     * @param userIds
     *            用户主键集合。
     * @param questionId
     *            考核题目主键。
     * @return 已有答案的用户主键列表。
     */
    List<Long> selectExistingAnswerUserIds(@Param("userIds") List<Long> userIds,
            @Param("questionId") Long questionId);

    /**
     * 统计用户在指定考核场次的个人 FILE_UPLOAD 作答数量。
     *
     * @param userId
     *            用户主键。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的记录数量。
     */
    int countPersonalAnswersByUserIdAndAssessmentTimeId(@Param("userId") Long userId,
            @Param("assessmentTimeId") Long assessmentTimeId);

    /**
     * 统计用户在指定考核场次的队伍 FILE_UPLOAD 作答数量。
     *
     * @param userId
     *            用户主键。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的记录数量。
     */
    int countTeamAnswersByUserIdAndAssessmentTimeId(@Param("userId") Long userId,
            @Param("assessmentTimeId") Long assessmentTimeId);
}
