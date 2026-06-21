package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AssessmentQuestionRepository {
    /**
     * 保存新的考核题目 记录。
     *
     * @param assessmentQuestion
     *            考核题目领域对象。
     */
    void save(AssessmentQuestion assessmentQuestion);
    /**
     * 按主键查询考核题目 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核题目 结果；不存在时为空。
     */
    Optional<AssessmentQuestion> findById(Long id);

    /**
     * 按附件文件主键查询关联的考核题目记录。
     *
     * @param attachmentId
     *            附件文件主键。
     * @return 查询到的考核题目结果；不存在时为空。
     */
    Optional<AssessmentQuestion> findByAttachmentId(Long attachmentId);
    /**
     * 更新考核题目附件文件关联。
     *
     * @param questionId
     *            考核题目主键。
     * @param attachmentId
     *            附件文件主键。
     * @return 数据库受影响行数。
     */
    int updateAttachmentId(Long questionId, Long attachmentId);
    /**
     * 统计满足条件的考核题目 记录数量。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的记录数量。
     */
    int countByAssessmentTimeId(Long assessmentTimeId);

    /**
     * 按考核场次主键批量统计题目数量。
     *
     * @param assessmentTimeIds
     *            考核场次主键列表。
     * @return 考核场次主键到题目数量的映射；缺失主键视为 0。
     */
    Map<Long, Integer> countByAssessmentTimeIds(List<Long> assessmentTimeIds);
    /**
     * 查询指定考核场次下的全部题目视图。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的考核题目 结果。
     */
    Page<AssessmentQuestion> findAllByTimeId(Long assessmentTimeId, Pageable pageable);
    /**
     * 更新已有考核题目 记录。
     *
     * @param question
     *            考核题目对象。
     */
    void update(AssessmentQuestion question);
    /**
     * 删除指定考核题目 记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);
    /**
     * 判断是否存在满足条件的考核题目 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);
    /**
     * 按考核场次和题号查询题目视图。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param questionNo
     *            题目在考核场次中的序号。
     * @return 查询到的考核题目 结果；不存在时为空。
     */
    Optional<AssessmentQuestion> findByTimeIdAndQuestionNo(Long assessmentTimeId, Integer questionNo);
}
