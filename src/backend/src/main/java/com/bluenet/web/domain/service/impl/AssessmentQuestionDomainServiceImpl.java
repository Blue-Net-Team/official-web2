package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
