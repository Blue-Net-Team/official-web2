package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.competition.CompetitionBriefDTO;
import com.bluenet.web.api.dto.competition.CompetitionDetailDTO;
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.model.vo.CompetitionVO;
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
    public CompetitionBriefDTO convertToBriefDTO(CompetitionBriefVO vo) {
        return CompetitionBriefDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .shortName(vo.getShortName())
                .logoUrl(vo.getLogoUrl())
                .logoFileId(vo.getLogoFileId())
                .coverFileId(vo.getCoverFileId())
                .summary(vo.getSummary())
                .level(vo.getLevel())
                .month(vo.getMonth())
                .organizer(vo.getOrganizer())
                .build();
    }

    public List<CompetitionBriefDTO> convertToBriefDTOList(List<CompetitionBriefVO> voList) {
        return voList.stream().map(this::convertToBriefDTO).collect(Collectors.toList());
    }

    public CompetitionResponseDTO convertToResponseDTO(CompetitionBriefVO vo) {
        return CompetitionResponseDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .level(vo.getLevel())
                .month(vo.getMonth())
                .organizer(vo.getOrganizer())
                .summary(vo.getSummary())
                .coverFileId(vo.getCoverFileId())
                .build();
    }

    public List<CompetitionResponseDTO> convertToResponseDTOList(List<CompetitionBriefVO> voList) {
        return voList.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    public CompetitionDetailDTO convertToDetailDTO(CompetitionVO vo) {
        return CompetitionDetailDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .shortName(vo.getShortName())
                .logoUrl(vo.getLogoUrl())
                .logoFileId(vo.getLogoFileId())
                .coverFileId(vo.getCoverFileId())
                .summary(vo.getSummary())
                .detail(vo.getDetail())
                .level(vo.getLevel())
                .month(vo.getMonth())
                .organizer(vo.getOrganizer())
                .build();
    }
}
