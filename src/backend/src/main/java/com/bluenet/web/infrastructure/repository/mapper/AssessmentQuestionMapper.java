package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentQuestionCountResult;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentQuestionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentQuestionMapper extends BaseMapper<AssessmentQuestionDO> {
    /**
     * 统计满足条件的考核题目 记录数量。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的记录数量。
     */
    long countByAssessmentTimeId(@Param("assessmentTimeId") Long assessmentTimeId);

    /**
     * 按考核场次主键批量统计题目数量。
     *
     * @param assessmentTimeIds
     *            考核场次主键列表。
     * @return 每个考核场次的题目数量结果列表。
     */
    List<AssessmentQuestionCountResult> countByAssessmentTimeIds(
            @Param("assessmentTimeIds") List<Long> assessmentTimeIds);

    /**
     * 查询考核题目 数据行。
     *
     * @param page
     *            分页请求或 MyBatis-Plus 分页对象。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 分页后的考核题目 结果。
     */
    IPage<AssessmentQuestionDO> selectPageByAssessmentTimeId(IPage<AssessmentQuestionDO> page,
            @Param("assessmentTimeId") Long assessmentTimeId);

    /**
     * 按条件查询考核题目 数据行。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param questionNo
     *            题目在考核场次中的序号。
     * @return 匹配条件的考核题目 数据行；不存在时为 null。
     */
    AssessmentQuestionDO selectByAssessmentTimeIdAndQuestionNo(@Param("assessmentTimeId") Long assessmentTimeId,
            @Param("questionNo") Integer questionNo);

    /**
     * 查询考核题目 数据行。
     *
     * @param attachmentId
     *            附件文件主键。
     * @return 匹配条件的考核题目 数据行；不存在时为 null。
     */
    AssessmentQuestionDO selectFirstByAttachmentId(@Param("attachmentId") Long attachmentId);
}
