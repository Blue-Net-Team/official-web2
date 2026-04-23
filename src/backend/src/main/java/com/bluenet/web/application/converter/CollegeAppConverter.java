package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.application.CollegeResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学院应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class CollegeAppConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
    public CollegeDTO toDTO(CollegeResult result) {
        return CollegeDTO.builder()
                .id(result.id())
                .name(result.name())
                .build();
    }

    /**
     * 将应用层结果列表转换为 API 响应 DTO 列表
     */
    public List<CollegeDTO> toDTOList(List<CollegeResult> results) {
        return results.stream()
                .map(this::toDTO)
                .toList();
    }
}
