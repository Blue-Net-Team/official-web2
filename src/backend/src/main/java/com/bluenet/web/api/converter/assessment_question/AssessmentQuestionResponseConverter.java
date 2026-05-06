package com.bluenet.web.api.converter.assessment_question;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.UserQuestionListResponse;
import com.bluenet.web.application.AssessmentQuestionResult;
import com.bluenet.web.application.UserQuestionListResult;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 考题响应转换器
 * <p>
 * 负责将考题领域对象/结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class AssessmentQuestionResponseConverter {

    /**
     * 将考题实体转换为 DTO
     */
    public AssessmentQuestionDTO toDTO(AssessmentQuestion entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentQuestionDTO.builder()
                .id(entity.getId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .questionNo(entity.getQuestionNo())
                .questionType(entity.getQuestionType())
                .title(entity.getTitle())
                .content(entity.getContent())
                .attachmentId(entity.getAttachmentId())
                .score(entity.getScore())
                .build();
    }

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
     * 将考题实体转换为 DTO（用户端，不包含 content）
     */
    public AssessmentQuestionDTO toDTOForUser(AssessmentQuestion entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentQuestionDTO.builder()
                .id(entity.getId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .questionNo(entity.getQuestionNo())
                .questionType(entity.getQuestionType())
                .title(entity.getTitle())
                .content(null)
                .attachmentId(entity.getAttachmentId())
                .score(entity.getScore())
                .build();
    }

    /**
     * 将考题结果转换为 DTO（用户端，不包含 content）
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
                .content(null)
                .attachmentId(result.attachmentId())
                .score(result.score())
                .answered(result.answered())
                .build();
    }

    /**
     * 将考题实体列表转换为 DTO 列表
     */
    public List<AssessmentQuestionDTO> toDTOList(List<AssessmentQuestion> entityList) {
        if (entityList == null) {
            return List.of();
        }
        return entityList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将考题实体列表转换为 DTO 列表（用户端）
     */
    public List<AssessmentQuestionDTO> toDTOListForUser(List<AssessmentQuestion> entityList) {
        if (entityList == null) {
            return List.of();
        }
        return entityList.stream()
                .map(this::toDTOForUser)
                .collect(Collectors.toList());
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
