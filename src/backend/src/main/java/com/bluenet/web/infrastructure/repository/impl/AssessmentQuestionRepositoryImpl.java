package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.converter.AssessmentQuestionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentQuestionCountResult;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentQuestionDO;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentQuestionRepositoryImpl implements AssessmentQuestionRepository {
    private final AssessmentQuestionMapper assessmentQuestionMapper;
    private final AssessmentQuestionRepositoryConverter assessmentQuestionRepositoryConverter;

    /**
     * 保存新的考核题目 记录。
     *
     * @param assessmentQuestion
     *            考核题目领域对象。
     */
    @Override
    public void save(AssessmentQuestion assessmentQuestion) {
        AssessmentQuestionDO dataObject = assessmentQuestionRepositoryConverter.toDataObject(assessmentQuestion);
        if (dataObject.getId() == null) {
            assessmentQuestionMapper.insert(dataObject);
            assessmentQuestion.setId(dataObject.getId());
        } else {
            assessmentQuestionMapper.updateById(dataObject);
        }
    }

    /**
     * 按主键查询考核题目 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核题目 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentQuestion> findById(Long id) {
        AssessmentQuestion question = assessmentQuestionRepositoryConverter.toEntity(
                assessmentQuestionMapper.selectById(id));
        if (question == null) {
            log.warn("assessment question not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(question);
    }

    /**
     * 按附件文件主键查询关联的考核题目记录。
     *
     * @param attachmentId
     *            附件文件主键。
     * @return 查询到的考核题目结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentQuestion> findByAttachmentId(Long attachmentId) {
        AssessmentQuestion question = assessmentQuestionRepositoryConverter.toEntity(
                assessmentQuestionMapper.selectFirstByAttachmentId(attachmentId));
        if (question == null) {
            return Optional.empty();
        }
        return Optional.of(question);
    }

    /**
     * 更新考核题目附件文件关联。
     *
     * @param questionId
     *            考核题目主键。
     * @param attachmentId
     *            附件文件主键。
     * @return 数据库受影响行数。
     */
    @Override
    public int updateAttachmentId(Long questionId, Long attachmentId) {
        AssessmentQuestion question = AssessmentQuestion.reconstruct(
                questionId,
                null,
                null,
                null,
                null,
                null,
                attachmentId,
                null);
        int influence = assessmentQuestionMapper.updateById(
                assessmentQuestionRepositoryConverter.toDataObject(question));
        if (influence == 0) {
            log.warn("更新题目附件失败，保存到数据库时没有影响任何行，questionId {}, attachmentId {}", questionId, attachmentId);
            throw new GlobalException("更新题目附件失败");
        }
        return influence;
    }

    /**
     * 统计满足条件的考核题目 记录数量。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的记录数量。
     */
    @Override
    public int countByAssessmentTimeId(Long assessmentTimeId) {
        return Math.toIntExact(assessmentQuestionMapper.countByAssessmentTimeId(assessmentTimeId));
    }

    /**
     * 按考核场次主键批量统计题目数量。
     *
     * @param assessmentTimeIds
     *            考核场次主键列表。
     * @return 考核场次主键到题目数量的映射；缺失主键视为 0。
     */
    @Override
    public Map<Long, Integer> countByAssessmentTimeIds(List<Long> assessmentTimeIds) {
        if (assessmentTimeIds == null || assessmentTimeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return assessmentQuestionMapper.countByAssessmentTimeIds(assessmentTimeIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                AssessmentQuestionCountResult::assessmentTimeId,
                                result -> Math.toIntExact(result.count())));
    }

    /**
     * 查询指定考核场次下的全部题目视图。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的考核题目 结果。
     */
    @Override
    public org.springframework.data.domain.Page<AssessmentQuestion> findAllByTimeId(
            Long assessmentTimeId, org.springframework.data.domain.Pageable pageable) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AssessmentQuestionDO> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber() + 1, pageable.getPageSize());
        com.baomidou.mybatisplus.core.metadata.IPage<AssessmentQuestionDO> result = assessmentQuestionMapper
                .selectPageByAssessmentTimeId(page, assessmentTimeId);
        List<AssessmentQuestion> content = assessmentQuestionRepositoryConverter.toEntityList(result.getRecords());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    /**
     * 更新已有考核题目 记录。
     *
     * @param question
     *            考核题目对象。
     */
    /**
     * 删除指定考核题目 记录。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    public void deleteById(Long id) {
        assessmentQuestionMapper.deleteById(id);
    }

    /**
     * 判断是否存在满足条件的考核题目 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsById(Long id) {
        return assessmentQuestionMapper.selectById(id) != null;
    }

    /**
     * 按考核场次和题号查询题目视图。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param questionNo
     *            题目在考核场次中的序号。
     * @return 查询到的考核题目 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentQuestion> findByTimeIdAndQuestionNo(Long assessmentTimeId, Integer questionNo) {
        AssessmentQuestion question = assessmentQuestionRepositoryConverter.toEntity(
                assessmentQuestionMapper.selectByAssessmentTimeIdAndQuestionNo(assessmentTimeId, questionNo));
        if (question == null) {
            return Optional.empty();
        }
        return Optional.of(question);
    }
}
