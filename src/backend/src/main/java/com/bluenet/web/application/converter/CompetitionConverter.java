package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.competition.CompetitionBriefDTO;
import com.bluenet.web.api.dto.competition.CompetitionDetailDTO;
import com.bluenet.web.api.dto.competition.CompetitionImageDTO;
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 竞赛转换器
 * <p>
 * 负责竞赛相关的VO与DTO之间的转换
 * </p>
 */
@Component
public class CompetitionConverter {
    /**
     * 将竞赛简要VO转换为DTO
     *
     * @param vo
     *            竞赛简要VO
     * @return 竞赛简要DTO
     */
    public CompetitionBriefDTO convertToBriefDTO(CompetitionBriefVO vo) {
        return CompetitionBriefDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .shortName(vo.getShortName())
                .logoUrl(vo.getLogoUrl())
                .logoFileId(vo.getLogoFileId())
                .summary(vo.getSummary())
                .level(vo.getLevel())
                .month(vo.getMonth())
                .organizer(vo.getOrganizer())
                .build();
    }

    /**
     * 将竞赛简要VO列表转换为DTO列表
     *
     * @param voList
     *            竞赛简要VO列表
     * @return 竞赛简要DTO列表
     */
    public List<CompetitionBriefDTO> convertToBriefDTOList(List<CompetitionBriefVO> voList) {
        return voList.stream().map(this::convertToBriefDTO).collect(Collectors.toList());
    }

    /**
     * 将竞赛简要VO转换为响应DTO
     *
     * @param vo
     *            竞赛简要VO
     * @return 竞赛响应DTO
     */
    public CompetitionResponseDTO convertToResponseDTO(CompetitionBriefVO vo) {
        return CompetitionResponseDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .level(vo.getLevel())
                .month(vo.getMonth())
                .organizer(vo.getOrganizer())
                .summary(vo.getSummary())
                .introduceImageFileId(vo.getIntroduceImageFileId())
                .build();
    }

    /**
     * 将竞赛简要VO列表转换为响应DTO列表
     *
     * @param voList
     *            竞赛简要VO列表
     * @return 竞赛响应DTO列表
     */
    public List<CompetitionResponseDTO> convertToResponseDTOList(List<CompetitionBriefVO> voList) {
        return voList.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    /**
     * 将竞赛详情VO转换为DTO
     *
     * @param vo
     *            竞赛详情VO
     * @param images
     *            竞赛相关图片VO列表
     * @return 竞赛详情DTO
     */
    public CompetitionDetailDTO convertToDetailDTO(CompetitionVO vo, List<IntroduceImageVO> images) {
        List<CompetitionImageDTO> imageDTOs = images.stream().map(this::convertToImageDTO).collect(Collectors.toList());

        return CompetitionDetailDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .shortName(vo.getShortName())
                .logoUrl(vo.getLogoUrl())
                .logoFileId(vo.getLogoFileId())
                .summary(vo.getSummary())
                .detail(vo.getDetail())
                .level(vo.getLevel())
                .month(vo.getMonth())
                .organizer(vo.getOrganizer())
                .images(imageDTOs)
                .build();
    }

    /**
     * 将介绍图片VO转换为竞赛图片DTO
     *
     * @param vo
     *            介绍图片VO
     * @return 竞赛图片DTO
     */
    public CompetitionImageDTO convertToImageDTO(IntroduceImageVO vo) {
        return CompetitionImageDTO.builder()
                .id(vo.getId())
                .url(vo.getFileUrl())
                .description(vo.getDescription())
                .build();
    }
}
