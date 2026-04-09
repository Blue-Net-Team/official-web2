package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentQuestionDomainServiceImpl implements AssessmentQuestionDomainService {
    private final AssessmentQuestionRepository assessmentQuestionRepository;

    @Override
    @Transactional
    public void updateAttachment(AssessmentQuestionVO question, FileVO file) {
        log.info("update attachment for question {}, file {}", question.getId(), file.getId());
        if (file.getId() == null) {
            log.warn("更新题目附件的时候，file id 不能为空，请先保存文件获取id");
            throw new GlobalException("更新题目附件失败：文件ID不能为空");
        }

        assessmentQuestionRepository.updateAttachmentId(question.getId(), file.getId());
        log.info("update attachment success for question {}", question.getId());
    }

    @Override
    public AssessmentQuestionVO getQuestionById(Long questionId) {
        Optional<AssessmentQuestionVO> assessmentQuestionVOOptional = assessmentQuestionRepository.findById(questionId);
        if (assessmentQuestionVOOptional.isEmpty()) {
            log.warn("题目不存在，ID: {}", questionId);
            throw new DataNotFound("题目不存在，ID: " + questionId);
        }
        return assessmentQuestionVOOptional.get();
    }

    @Override
    @Transactional
    public AssessmentQuestionVO createQuestion(AssessmentQuestionVO question) {
        log.info(
                "create question for timeId {}, questionNo {}",
                question.getAssessmentTimeId(),
                question.getQuestionNo());

        // 检查题号是否重复
        Optional<AssessmentQuestionVO> existing = assessmentQuestionRepository.findByTimeIdAndQuestionNo(
                question.getAssessmentTimeId(),
                question.getQuestionNo());
        if (existing.isPresent()) {
            throw new DataConflict("该考核时间下题号 " + question.getQuestionNo() + " 已存在");
        }

        AssessmentQuestion entity = new AssessmentQuestion();
        entity.setAssessmentTimeId(question.getAssessmentTimeId());
        entity.setQuestionNo(question.getQuestionNo());
        entity.setQuestionType(question.getQuestionType());
        entity.setTitle(question.getTitle());
        entity.setContent(question.getContent());
        entity.setAttachmentId(question.getAttachmentId());
        entity.setScore(question.getScore());
        assessmentQuestionRepository.save(entity);

        return assessmentQuestionRepository.findById(entity.getId())
                .orElseThrow(() -> new GlobalException("创建考题失败"));
    }

    @Override
    @Transactional
    public AssessmentQuestionVO updateQuestion(AssessmentQuestionVO question) {
        log.info("update question id {}", question.getId());

        if (!assessmentQuestionRepository.existsById(question.getId())) {
            throw new DataNotFound("考题不存在，ID: " + question.getId());
        }

        // 如果修改了题号，检查新题号是否重复
        if (question.getQuestionNo() != null) {
            Optional<AssessmentQuestionVO> existing = assessmentQuestionRepository.findByTimeIdAndQuestionNo(
                    question.getAssessmentTimeId(),
                    question.getQuestionNo());
            if (existing.isPresent() && !existing.get().getId().equals(question.getId())) {
                throw new DataConflict("该考核时间下题号 " + question.getQuestionNo() + " 已存在");
            }
        }

        assessmentQuestionRepository.update(question);
        return assessmentQuestionRepository.findById(question.getId())
                .orElseThrow(() -> new GlobalException("更新考题失败"));
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        log.info("delete question id {}", id);

        if (!assessmentQuestionRepository.existsById(id)) {
            throw new DataNotFound("考题不存在，ID: " + id);
        }

        assessmentQuestionRepository.deleteById(id);
        log.info("delete question success id {}", id);
    }

    @Override
    public Page<AssessmentQuestionVO> listQuestions(Long assessmentTimeId, Pageable pageable) {
        log.info(
                "list questions for timeId {}, page {}, size {}",
                assessmentTimeId,
                pageable.getPageNumber(),
                pageable.getPageSize());
        return assessmentQuestionRepository.findAllByTimeId(assessmentTimeId, pageable);
    }
}
