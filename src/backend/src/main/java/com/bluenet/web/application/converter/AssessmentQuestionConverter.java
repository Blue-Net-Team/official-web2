package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
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
public class AssessmentQuestionConverter {
    /**
     * 将考题VO转换为DTO
     */
    public AssessmentQuestionDTO convertToDTO(AssessmentQuestionVO vo) {
        return AssessmentQuestionDTO.builder()
                .id(vo.getId())
                .assessmentTimeId(vo.getAssessmentTimeId())
                .questionNo(vo.getQuestionNo())
                .questionType(vo.getQuestionType())
                .title(vo.getTitle())
                .content(vo.getContent())
                .attachmentId(vo.getAttachmentId())
                .score(vo.getScore())
                .build();
    }

    /**
     * 将考题VO转换为DTO（用户端，不包含content）
     */
    public AssessmentQuestionDTO convertToDTOForUser(AssessmentQuestionVO vo) {
        return AssessmentQuestionDTO.builder()
                .id(vo.getId())
                .assessmentTimeId(vo.getAssessmentTimeId())
                .questionNo(vo.getQuestionNo())
                .questionType(vo.getQuestionType())
                .title(vo.getTitle())
                .content(null) // 用户端不返回题目内容
                .attachmentId(vo.getAttachmentId())
                .score(vo.getScore())
                .build();
    }

    /**
     * 将考题VO列表转换为DTO列表
     */
    public List<AssessmentQuestionDTO> convertToDTOList(List<AssessmentQuestionVO> voList) {
        return voList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将考题VO列表转换为DTO列表（用户端）
     */
    public List<AssessmentQuestionDTO> convertToDTOListForUser(List<AssessmentQuestionVO> voList) {
        return voList.stream()
                .map(this::convertToDTOForUser)
                .collect(Collectors.toList());
    }
}
