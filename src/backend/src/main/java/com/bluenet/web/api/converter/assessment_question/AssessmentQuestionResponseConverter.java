package com.bluenet.web.api.converter.assessment_question;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.UserQuestionListResponse;
import com.bluenet.web.application.result.assessment.AssessmentQuestionResult;
import com.bluenet.web.application.result.user.UserQuestionListResult;
import org.springframework.stereotype.Component;

/**
 * 考题响应转换器
 * <p>
 * 负责将考题应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class AssessmentQuestionResponseConverter {

    /**
     * 将考题结果转换为 DTO
     */
    public AssessmentQuestionDTO toDTO(AssessmentQuestionResult result) {
        if (result == null) {
            return null;
        }
        return AssessmentQuestionDTO.builder()
                .id(result.id())
                .assessmentTimeId(result.assessmentTimeId())
                .questionNo(result.questionNo())
                .questionType(result.questionType())
                .title(result.title())
                .content(result.content())
                .attachmentId(result.attachmentId())
                .score(result.score())
                .answered(result.answered())
                .build();
    }

    /**
     * 将考题结果转换为 DTO（用户端）
     */
    public AssessmentQuestionDTO toDTOForUser(AssessmentQuestionResult result) {
        if (result == null) {
            return null;
        }
        return AssessmentQuestionDTO.builder()
                .id(result.id())
                .assessmentTimeId(result.assessmentTimeId())
                .questionNo(result.questionNo())
                .questionType(result.questionType())
                .title(result.title())
                .content(result.content())
                .attachmentId(result.attachmentId())
                .score(result.score())
                .answered(result.answered())
                .build();
    }

    /**
     * 将用户考题列表结果转换为响应 DTO
     */
    public UserQuestionListResponse toResponse(UserQuestionListResult result) {
        if (result == null) {
            return null;
        }
        return UserQuestionListResponse.builder()
                .questions(PageDTO.from(result.questions().map(this::toDTOForUser)))
                .deadline(result.deadline())
                .ended(result.ended())
                .build();
    }
}
