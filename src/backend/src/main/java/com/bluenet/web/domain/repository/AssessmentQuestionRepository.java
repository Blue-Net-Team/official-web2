package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;

import java.util.Optional;

public interface AssessmentQuestionRepository {
    void save(AssessmentQuestion assessmentQuestion);
    Optional<AssessmentQuestionVO> findById(Long id);
    int updateAttachmentId(Long questionId, Long attachmentId);
    int countByAssessmentTimeId(Long assessmentTimeId);
}
