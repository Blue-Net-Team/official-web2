package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.domain.model.vo.CollegeVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学院转换器
 * <p>
 * 负责学院相关的VO与DTO之间的转换
 * </p>
 */
@Component
public class CollegeConverter {
    /**
     * 将学院VO转换为DTO
     *
     * @param vo
     *            学院VO
     * @return 学院DTO
     */
    public CollegeDTO convertToDTO(CollegeVO vo) {
        return CollegeDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .build();
    }

    /**
     * 将学院VO列表转换为DTO列表
     *
     * @param voList
     *            学院VO列表
     * @return 学院DTO列表
     */
    public List<CollegeDTO> convertToDTOList(List<CollegeVO> voList) {
        return voList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
