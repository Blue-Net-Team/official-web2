package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.competition.*;
import com.bluenet.web.application.converter.CompetitionConverter;
import com.bluenet.web.application.service.CompetitionService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
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
        List<CompetitionBriefVO> voList = competitionDomainService.getCompetitionList(validLimit);
        return competitionConverter.convertToResponseDTOList(voList);
    }

    @Override
    @Transactional
    public CompetitionResponseDTO createCompetition(CompetitionRequestDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("竞赛名称不能为空");
        }
        Long id = competitionDomainService.createCompetition(
                request.getName(),
                request.getShortName(),
                request.getLogoFileId(),
                request.getSummary(),
                request.getLevel(),
                request.getMonth(),
                request.getOrganizer());

        CompetitionBriefVO briefVO = CompetitionBriefVO.builder()
                .id(id)
                .name(request.getName())
                .shortName(request.getShortName())
                .logoFileId(request.getLogoFileId())
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

        competitionDomainService.updateCompetition(
                id,
                request.getName(),
                request.getShortName(),
                request.getLogoFileId(),
                request.getSummary(),
                request.getLevel(),
                request.getMonth(),
                request.getOrganizer());

        List<CompetitionBriefVO> voList = competitionDomainService.getCompetitionList(50);
        CompetitionBriefVO updated = voList.stream()
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

    @Override
    @Transactional
    public void updateLogo(Long id, Long fileId) {
        if (!competitionDomainService.existsById(id)) {
            throw new DataNotFound("竞赛不存在");
        }
        FileVO fileVO = fileDomainService.getFileById(fileId);
        if (fileVO == null) {
            throw new DataNotFound("文件不存在");
        }
        if (fileVO.getType() != FileType.NORMAL_IMG) {
            throw new BadRequest("文件类型不匹配，期望 NORMAL_IMG");
        }
        competitionDomainService.updateLogo(id, fileId);
        log.info("竞赛Logo更新成功 - competitionId={}, fileId={}", id, fileId);
    }

    @Override
    @Transactional
    public void updateCover(Long id, Long fileId) {
        if (!competitionDomainService.existsById(id)) {
            throw new DataNotFound("竞赛不存在");
        }
        FileVO fileVO = fileDomainService.getFileById(fileId);
        if (fileVO == null) {
            throw new DataNotFound("文件不存在");
        }
        if (fileVO.getType() != FileType.NORMAL_IMG) {
            throw new BadRequest("文件类型不匹配，期望 NORMAL_IMG");
        }
        competitionDomainService.updateCover(id, fileId);
        log.info("竞赛封面更新成功 - competitionId={}, fileId={}", id, fileId);
    }
}
