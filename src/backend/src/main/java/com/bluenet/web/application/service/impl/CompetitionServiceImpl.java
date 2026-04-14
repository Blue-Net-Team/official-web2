package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.competition.*;
import com.bluenet.web.application.converter.CompetitionConverter;
import com.bluenet.web.application.service.CompetitionService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.CompetitionDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {
    private final CompetitionDomainService competitionDomainService;
    private final CompetitionConverter competitionConverter;
    private final FileDomainService fileDomainService;

    @Override
    public List<CompetitionResponseDTO> getCompetitionResponseList(int limit) {
        int validLimit = Math.min(Math.max(limit, 1), 50);
        List<CompetitionVO> voList = competitionDomainService.getCompetitionList(validLimit);
        return competitionConverter.convertToResponseDTOList(voList);
    }

    @Override
    @Transactional
    public CompetitionResponseDTO createCompetition(CompetitionRequestDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("竞赛名称不能为空");
        }
        validateFileId(request.getLogoFileId(), "Logo");
        validateFileId(request.getCoverFileId(), "封面");
        Long id = competitionDomainService.createCompetition(
                request.getName(),
                request.getShortName(),
                request.getLogoFileId(),
                request.getCoverFileId(),
                request.getSummary(),
                request.getLevel(),
                request.getMonth(),
                request.getOrganizer());

        CompetitionVO briefVO = CompetitionVO.builder()
                .id(id)
                .name(request.getName())
                .shortName(request.getShortName())
                .logoFileId(request.getLogoFileId())
                .coverFileId(request.getCoverFileId())
                .summary(request.getSummary())
                .level(request.getLevel() != null ? request.getLevel() : "省级")
                .month(request.getMonth())
                .organizer(request.getOrganizer())
                .build();
        return competitionConverter.convertToResponseDTO(briefVO);
    }

    @Override
    @Transactional
    public CompetitionResponseDTO updateCompetition(Long id, CompetitionRequestDTO request) {
        if (!competitionDomainService.existsById(id)) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        validateFileId(request.getLogoFileId(), "Logo");
        validateFileId(request.getCoverFileId(), "封面");

        competitionDomainService.updateCompetition(
                id,
                request.getName(),
                request.getShortName(),
                request.getLogoFileId(),
                request.getCoverFileId(),
                request.getSummary(),
                request.getLevel(),
                request.getMonth(),
                request.getOrganizer());

        List<CompetitionVO> voList = competitionDomainService.getCompetitionList(50);
        CompetitionVO updated = voList.stream()
                .filter(vo -> vo.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new GlobalException("更新竞赛失败"));

        return competitionConverter.convertToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCompetition(Long id) {
        if (!competitionDomainService.existsById(id)) {
            throw new IllegalArgumentException("竞赛不存在");
        }
        competitionDomainService.deleteCompetition(id);
    }

    @Override
    @Transactional
    public void updateSortOrder(Long id, UpdateSortOrderRequestDTO request) {
        if (!competitionDomainService.existsById(id)) {
            throw new IllegalArgumentException("竞赛不存在");
        }
        competitionDomainService.updateSortOrder(id, request.getSortOrder());
    }

    private void validateFileId(Long fileId, String fieldName) {
        if (fileId == null) {
            return;
        }
        FileVO fileVO = fileDomainService.getFileById(fileId);
        if (fileVO == null) {
            throw new DataNotFound(fieldName + "文件不存在");
        }
        if (fileVO.getType() != FileType.NORMAL_IMG) {
            throw new BadRequest(fieldName + "文件类型不匹配，期望 NORMAL_IMG");
        }
    }
}
