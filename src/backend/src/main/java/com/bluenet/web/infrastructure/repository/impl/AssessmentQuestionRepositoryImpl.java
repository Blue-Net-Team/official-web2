package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentQuestionRepositoryImpl implements AssessmentQuestionRepository {
    private final AssessmentQuestionMapper assessmentQuestionMapper;

    /**
     * 保存新的考核题目 记录。
     *
     * @param assessmentQuestion
     *            考核题目领域对象。
     */
    @Override
    public void save(AssessmentQuestion assessmentQuestion) {
        log.info("save assessment question {}", assessmentQuestion);
        RepositoryObjectConverter.insert(assessmentQuestionMapper, assessmentQuestion, AssessmentQuestionDO.class);
    }

    /**
     * 按主键查询考核题目 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核题目 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentQuestionVO> findById(Long id) {
        AssessmentQuestion question = RepositoryObjectConverter
                .toDomain(assessmentQuestionMapper.selectById(id), AssessmentQuestion.class);
        if (question == null) {
            log.warn("assessment question not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(question));
    }

    /**
     * 按附件文件主键查询关联的考核题目记录。
     *
     * @param attachmentId
     *            附件文件主键。
     * @return 查询到的考核题目结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentQuestionVO> findByAttachmentId(Long attachmentId) {
        AssessmentQuestion question = RepositoryObjectConverter.toDomain(
                assessmentQuestionMapper.selectFirstByAttachmentId(attachmentId),
                AssessmentQuestion.class);
        if (question == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(question));
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
        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(questionId);
        question.setAttachmentId(attachmentId);
        int influence = RepositoryObjectConverter
                .updateById(assessmentQuestionMapper, question, AssessmentQuestionDO.class);
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
     * 查询指定考核场次下的全部题目视图。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的考核题目 结果。
     */
    @Override
    public org.springframework.data.domain.Page<AssessmentQuestionVO> findAllByTimeId(
            Long assessmentTimeId, org.springframework.data.domain.Pageable pageable) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AssessmentQuestionDO> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber() + 1, pageable.getPageSize());
        com.baomidou.mybatisplus.core.metadata.IPage<AssessmentQuestionDO> result = assessmentQuestionMapper
                .selectPageByAssessmentTimeId(page, assessmentTimeId);
        List<AssessmentQuestionVO> content = result.getRecords()
                .stream()
                .map(question -> convertToVO(RepositoryObjectConverter.toDomain(question, AssessmentQuestion.class)))
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    /**
     * 更新已有考核题目 记录。
     *
     * @param question
     *            考核题目对象。
     */
    @Override
    public void update(AssessmentQuestionVO question) {
        AssessmentQuestion entity = new AssessmentQuestion();
        entity.setId(question.getId());
        if (question.getQuestionNo() != null) {
            entity.setQuestionNo(question.getQuestionNo());
        }
        if (question.getQuestionType() != null) {
            entity.setQuestionType(question.getQuestionType());
        }
        if (question.getTitle() != null) {
            entity.setTitle(question.getTitle());
        }
        if (question.getContent() != null) {
            entity.setContent(question.getContent());
        }
        if (question.getAttachmentId() != null) {
            entity.setAttachmentId(question.getAttachmentId());
        }
        if (question.getScore() != null) {
            entity.setScore(question.getScore());
        }
        RepositoryObjectConverter.updateById(assessmentQuestionMapper, entity, AssessmentQuestionDO.class);
    }

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
    public Optional<AssessmentQuestionVO> findByTimeIdAndQuestionNo(Long assessmentTimeId, Integer questionNo) {
        AssessmentQuestion question = RepositoryObjectConverter.toDomain(
                assessmentQuestionMapper.selectByAssessmentTimeIdAndQuestionNo(assessmentTimeId, questionNo),
                AssessmentQuestion.class);
        if (question == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(question));
    }

    /**
     * 在考核题目 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param question
     *            考核题目对象。
     * @return 转换后的目标模型对象。
     */
    private AssessmentQuestionVO convertToVO(AssessmentQuestion question) {
        return AssessmentQuestionVO.builder()
                .id(question.getId())
                .assessmentTimeId(question.getAssessmentTimeId())
                .questionNo(question.getQuestionNo())
                .questionType(question.getQuestionType())
                .title(question.getTitle())
                .content(question.getContent())
                .attachmentId(question.getAttachmentId())
                .score(question.getScore())
                .build();
    }
}
