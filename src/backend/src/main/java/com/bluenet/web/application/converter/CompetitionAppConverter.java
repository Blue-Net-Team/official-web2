package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 竞赛应用层转换器
 * <p>
 * 负责竞赛相关的VO与DTO之间的转换
 * </p>
 */
@Component
public class CompetitionAppConverter {
    public CompetitionResponseDTO convertToResponseDTO(CompetitionVO vo) {
        return CompetitionResponseDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .shortName(vo.getShortName())
                .level(vo.getLevel())
                .month(vo.getMonth())
                .organizer(vo.getOrganizer())
                .summary(vo.getSummary())
                .logoFileId(vo.getLogoFileId())
                .coverFileId(vo.getCoverFileId())
                .sortOrder(vo.getSortOrder())
                .build();
    }

    public List<CompetitionResponseDTO> convertToResponseDTOList(List<CompetitionVO> voList) {
        return voList.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    public Page<CompetitionResponseDTO> convertToDTOPage(Page<CompetitionVO> voPage) {
        return voPage.map(this::convertToResponseDTO);
    }
}
