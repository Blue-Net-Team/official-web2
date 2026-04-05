package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AssessmentQuestionRepository {
    void save(AssessmentQuestion assessmentQuestion);
    Optional<AssessmentQuestionVO> findById(Long id);
    int updateAttachmentId(Long questionId, Long attachmentId);
    int countByAssessmentTimeId(Long assessmentTimeId);
    Page<AssessmentQuestionVO> findAllByTimeId(Long assessmentTimeId, Pageable pageable);
    void update(AssessmentQuestionVO question);
    void deleteById(Long id);
    boolean existsById(Long id);
    Optional<AssessmentQuestionVO> findByTimeIdAndQuestionNo(Long assessmentTimeId, Integer questionNo);
}
