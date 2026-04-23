package com.bluenet.web.application.converter;

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
 * 考题转换器
 * <p>
 * 负责考题VO与DTO之间的转换
 * </p>
 */
@Component
public class AssessmentQuestionAppConverter {
    /**
     * 将考题实体转换为DTO
     */
    public AssessmentQuestionDTO convertToDTO(AssessmentQuestion entity) {
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
     * 将考题结果转换为DTO
     */
    public AssessmentQuestionDTO convertToDTO(AssessmentQuestionResult result) {
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
     * 将考题实体转换为DTO（用户端，不包含content）
     */
    public AssessmentQuestionDTO convertToDTOForUser(AssessmentQuestion entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentQuestionDTO.builder()
                .id(entity.getId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .questionNo(entity.getQuestionNo())
                .questionType(entity.getQuestionType())
                .title(entity.getTitle())
                .content(null) // 用户端不返回题目内容
                .attachmentId(entity.getAttachmentId())
                .score(entity.getScore())
                .build();
    }

    /**
     * 将考题结果转换为DTO（用户端，不包含content）
     */
    public AssessmentQuestionDTO convertToDTOForUser(AssessmentQuestionResult result) {
        if (result == null) {
            return null;
        }
        return AssessmentQuestionDTO.builder()
                .id(result.id())
                .assessmentTimeId(result.assessmentTimeId())
                .questionNo(result.questionNo())
                .questionType(result.questionType())
                .title(result.title())
                .content(null) // 用户端不返回题目内容
                .attachmentId(result.attachmentId())
                .score(result.score())
                .answered(result.answered())
                .build();
    }

    /**
     * 将考题实体列表转换为DTO列表
     */
    public List<AssessmentQuestionDTO> convertToDTOList(List<AssessmentQuestion> entityList) {
        if (entityList == null) {
            return List.of();
        }
        return entityList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将考题VO列表转换为DTO列表（用户端）
     */
    public List<AssessmentQuestionDTO> convertToDTOListForUser(List<AssessmentQuestion> entityList) {
        if (entityList == null) {
            return List.of();
        }
        return entityList.stream()
                .map(this::convertToDTOForUser)
                .collect(Collectors.toList());
    }

    /**
     * 将用户考题列表结果转换为响应DTO
     */
    public UserQuestionListResponse convertToResponse(UserQuestionListResult result) {
        if (result == null) {
            return null;
        }
        return UserQuestionListResponse.builder()
                .questions(PageDTO.from(result.questions().map(this::convertToDTOForUser)))
                .deadline(result.deadline())
                .build();
    }
}
