package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.introduce.IntroduceImageDTO;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 介绍图片转换器
 * <p>
 * 负责介绍图片相关的VO与DTO之间的转换
 * </p>
 */
@Component
public class IntroduceImageConverter {
    /**
     * 将介绍图片VO转换为DTO
     *
     * @param introduceImageVO
     *            介绍图片VO
     * @return 介绍图片DTO
     */
    public IntroduceImageDTO convertToIntroduceImageDTO(IntroduceImageVO introduceImageVO) {
        return IntroduceImageDTO.builder()
                .id(introduceImageVO.getId())
                .type(introduceImageVO.getType())
                .description(introduceImageVO.getDescription())
                .fileId(introduceImageVO.getFileId())
                .direction(introduceImageVO.getDirection())
                .fileUrl(introduceImageVO.getFileUrl())
                .build();
    }

    /**
     * 将介绍图片VO列表转换为DTO列表
     *
     * @param introduceImageVOList
     *            介绍图片VO列表
     * @return 介绍图片DTO列表
     */
    public List<IntroduceImageDTO> convertToIntroduceImageDTOList(List<IntroduceImageVO> introduceImageVOList) {
        return introduceImageVOList.stream().map(this::convertToIntroduceImageDTO).collect(Collectors.toList());
    }
}
