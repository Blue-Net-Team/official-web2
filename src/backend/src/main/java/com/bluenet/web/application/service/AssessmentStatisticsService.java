package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.assessment_statistics.QuestionStatisticsDTO;

public interface AssessmentStatisticsService {
    QuestionStatisticsDTO getQuestionStatistics(Long questionId);

    /**
     * Query aggregate statistics for the candidate-facing question page when the
     * feature is enabled.
     *
     * @param questionId
     *            question id
     * @return aggregate statistics visible to the current candidate
     */
    QuestionStatisticsDTO getCandidateQuestionStatistics(Long questionId);
}
