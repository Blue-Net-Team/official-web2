package com.bluenet.web.api.converter.competition;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.domain.model.readmodel.CompetitionReadModel;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 竞赛响应转换器
 * <p>
 * 负责将竞赛 VO 转换为接口 DTO
 * </p>
 */
@Component
public class CompetitionResponseConverter {

    public CompetitionResponseDTO toDTO(CompetitionReadModel vo) {
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

    public List<CompetitionResponseDTO> toDTOList(List<CompetitionReadModel> voList) {
        return voList.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Page<CompetitionResponseDTO> toDTOPage(Page<CompetitionReadModel> voPage) {
        return voPage.map(this::toDTO);
    }

    public PageDTO<CompetitionResponseDTO> toPageDTO(Page<CompetitionReadModel> voPage) {
        return PageDTO.from(voPage.map(this::toDTO));
    }
}
