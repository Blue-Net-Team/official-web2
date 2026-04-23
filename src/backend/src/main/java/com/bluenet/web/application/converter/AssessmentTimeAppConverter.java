package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 考核时间应用层转换器
 * <p>
 * 负责应用层 Entity 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class AssessmentTimeAppConverter {
    /**
     * 将考核时间实体转换为 DTO
     *
     * @param entity
     *            考核时间实体
     * @return 考核时间 DTO
     */
    public AssessmentTimeDTO convertToDTO(AssessmentTime entity) {
        return AssessmentTimeDTO.builder()
                .id(entity.getId())
                .direction(entity.getDirection())
                .epoch(entity.getEpoch())
                .grade(entity.getGrade())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .timeLimit(entity.getTimeLimit())
                .timeLimitMinutes(entity.getTimeLimitMinutes())
                .build();
    }

    /**
     * 将考核时间实体列表转换为 DTO 列表
     *
     * @param entityList
     *            考核时间实体列表
     * @return 考核时间 DTO 列表
     */
    public List<AssessmentTimeDTO> convertToDTOList(List<AssessmentTime> entityList) {
        return entityList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
