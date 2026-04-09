package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentAnswerDomainServiceImpl implements AssessmentAnswerDomainService {
    private final AssessmentAnswerRepository assessmentAnswerRepository;

    @Override
    @Transactional
    public void updateWorkFile(AssessmentAnswerVO answer, FileVO file) {
        log.info("update work file for answer {}, file {}", answer.getId(), file.getId());
        if (file.getId() == null) {
            log.warn("更新答题工作文件的时候，file id 不能为空，请先保存文件获取id");
            throw new GlobalException("更新答题工作文件失败：文件ID不能为空");
        }

        // 更新文件ID
        assessmentAnswerRepository.updateFileId(answer.getId(), file.getId());

        // 更新提交时间
        assessmentAnswerRepository.updateSubmitTime(answer.getId(), LocalDateTime.now());
        log.info("update work file success for answer {}", answer.getId());
    }

    @Override
    public AssessmentAnswerVO getAnswerById(Long answerId) {
        return assessmentAnswerRepository.findById(answerId)
                .orElseThrow(() -> {
                    log.warn("答题不存在，ID: {}", answerId);
                    return new GlobalException("答题不存在，ID: " + answerId);
                });
    }

    @Override
    @Transactional
    public AssessmentAnswerVO createAnswer(AssessmentAnswerVO answer) {
        if (assessmentAnswerRepository.existsByUserIdAndQuestionId(answer.getUserId(), answer.getQuestionId())) {
            throw new DataConflict("已经提交过该题目的答案");
        }

        AssessmentAnswer entity = new AssessmentAnswer();
        entity.setUserId(answer.getUserId());
        entity.setQuestionId(answer.getQuestionId());
        entity.setContent(answer.getContent());
        entity.setLanguage(answer.getLanguage());
        entity.setFileId(answer.getFileId());
        entity.setSubmitTime(LocalDateTime.now());

        assessmentAnswerRepository.save(entity);

        log.info(
                "创建答案成功，userId: {}, questionId: {}, answerId: {}",
                answer.getUserId(),
                answer.getQuestionId(),
                entity.getId());

        return AssessmentAnswerVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .questionId(entity.getQuestionId())
                .content(entity.getContent())
                .language(entity.getLanguage())
                .fileId(entity.getFileId())
                .submitTime(entity.getSubmitTime())
                .build();
    }

    @Override
    @Transactional
    public void updateAnswer(AssessmentAnswerVO answer, Long fileId, String content) {
        log.info(
                "update answer {}, fileId: {}, content length: {}",
                answer.getId(),
                fileId,
                content != null ? content.length() : 0);

        if (fileId != null) {
            assessmentAnswerRepository.updateFileId(answer.getId(), fileId);
        }

        if (content != null) {
            assessmentAnswerRepository.updateContent(answer.getId(), content);
        }

        assessmentAnswerRepository.updateSubmitTime(answer.getId(), LocalDateTime.now());
        log.info("update answer success for answer {}", answer.getId());
    }
}
