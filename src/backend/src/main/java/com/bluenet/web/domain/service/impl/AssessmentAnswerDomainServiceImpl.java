package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.GlobalException;
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
}
