package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 考核时间转换器
 * <p>
 * 负责考核时间相关的VO与DTO之间的转换
 * </p>
 */
@Component
public class AssessmentTimeConverter {
    /**
     * 将考核时间VO转换为DTO
     *
     * @param vo
     *            考核时间VO
     * @return 考核时间DTO
     */
    public AssessmentTimeDTO convertToDTO(AssessmentTimeVO vo) {
        return AssessmentTimeDTO.builder()
                .id(vo.getId())
                .direction(vo.getDirection())
                .epoch(vo.getEpoch())
                .grade(vo.getGrade())
                .startTime(vo.getStartTime())
                .endTime(vo.getEndTime())
                .timeLimit(vo.getTimeLimit())
                .timeLimitMinutes(vo.getTimeLimitMinutes())
                .build();
    }

    /**
     * 将考核时间VO列表转换为DTO列表
     *
     * @param voList
     *            考核时间VO列表
     * @return 考核时间DTO列表
     */
    public List<AssessmentTimeDTO> convertToDTOList(List<AssessmentTimeVO> voList) {
        return voList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
