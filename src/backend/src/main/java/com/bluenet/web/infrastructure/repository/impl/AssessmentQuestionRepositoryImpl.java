package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
