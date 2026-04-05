package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @Override
    public void save(AssessmentQuestion assessmentQuestion) {
        log.info("save assessment question {}", assessmentQuestion);
        assessmentQuestionMapper.insert(assessmentQuestion);
    }

    @Override
    public Optional<AssessmentQuestionVO> findById(Long id) {
        AssessmentQuestion question = assessmentQuestionMapper.selectById(id);
        if (question == null) {
            log.warn("assessment question not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(question));
    }

    @Override
    public int updateAttachmentId(Long questionId, Long attachmentId) {
        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(questionId);
        question.setAttachmentId(attachmentId);
        int influence = assessmentQuestionMapper.updateById(question);
        if (influence == 0) {
            log.warn("更新题目附件失败，保存到数据库时没有影响任何行，questionId {}, attachmentId {}", questionId, attachmentId);
            throw new GlobalException("更新题目附件失败");
        }
        return influence;
    }

    @Override
    public int countByAssessmentTimeId(Long assessmentTimeId) {
        LambdaQueryWrapper<AssessmentQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentQuestion::getAssessmentTimeId, assessmentTimeId);
        return Math.toIntExact(assessmentQuestionMapper.selectCount(wrapper));
    }

    @Override
    public org.springframework.data.domain.Page<AssessmentQuestionVO> findAllByTimeId(
            Long assessmentTimeId, org.springframework.data.domain.Pageable pageable) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AssessmentQuestion> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<AssessmentQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentQuestion::getAssessmentTimeId, assessmentTimeId)
                .orderByAsc(AssessmentQuestion::getQuestionNo);
        com.baomidou.mybatisplus.core.metadata.IPage<AssessmentQuestion> result = assessmentQuestionMapper
                .selectPage(page, wrapper);
        List<AssessmentQuestionVO> content = result.getRecords()
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

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
        assessmentQuestionMapper.updateById(entity);
    }

    @Override
    public void deleteById(Long id) {
        assessmentQuestionMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return assessmentQuestionMapper.selectById(id) != null;
    }

    @Override
    public Optional<AssessmentQuestionVO> findByTimeIdAndQuestionNo(Long assessmentTimeId, Integer questionNo) {
        LambdaQueryWrapper<AssessmentQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentQuestion::getAssessmentTimeId, assessmentTimeId)
                .eq(AssessmentQuestion::getQuestionNo, questionNo);
        AssessmentQuestion question = assessmentQuestionMapper.selectOne(wrapper);
        if (question == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(question));
    }

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
