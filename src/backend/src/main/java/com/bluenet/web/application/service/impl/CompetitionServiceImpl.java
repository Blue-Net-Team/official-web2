package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.competition.*;
import com.bluenet.web.application.converter.CompetitionConverter;
import com.bluenet.web.application.service.CompetitionService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.CompetitionDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {
    private final CompetitionDomainService competitionDomainService;
    private final CompetitionConverter competitionConverter;
    private final FileDomainService fileDomainService;

    @Override
    public List<CompetitionBriefDTO> getCompetitionList(int limit) {
        int validLimit = Math.min(Math.max(limit, 1), 50);
        List<CompetitionBriefVO> voList = competitionDomainService.getCompetitionList(validLimit);
        return competitionConverter.convertToBriefDTOList(voList);
    }

    @Override
    public List<CompetitionResponseDTO> getCompetitionResponseList(int limit) {
        int validLimit = Math.min(Math.max(limit, 1), 50);
        List<CompetitionBriefVO> voList = competitionDomainService.getCompetitionList(validLimit);
        return competitionConverter.convertToResponseDTOList(voList);
    }

    @Override
    public CompetitionDetailDTO getCompetitionDetail(Long id) {
        Optional<CompetitionVO> competitionOpt = competitionDomainService.getCompetitionById(id);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }
        return competitionConverter.convertToDetailDTO(competitionOpt.get());
    }

    @Override
    @Transactional
    public CompetitionBriefDTO createCompetition(CreateCompetitionRequestDTO request) {
        Long id = competitionDomainService.createCompetition(
                request.getName(),
                request.getShortName(),
                request.getLogoFileId(),
                request.getSummary(),
                request.getDetail(),
                request.getLevel(),
                request.getMonth(),
                request.getOrganizer());

        Optional<CompetitionVO> created = competitionDomainService.getCompetitionById(id);
        if (created.isEmpty()) {
            throw new GlobalException("创建竞赛失败");
        }

        CompetitionBriefVO briefVO = CompetitionBriefVO.builder()
                .id(created.get().getId())
                .name(created.get().getName())
                .shortName(created.get().getShortName())
                .logoUrl(created.get().getLogoUrl())
                .logoFileId(created.get().getLogoFileId())
                .coverFileId(created.get().getCoverFileId())
                .summary(created.get().getSummary())
                .level(created.get().getLevel())
                .month(created.get().getMonth())
                .organizer(created.get().getOrganizer())
                .build();
        return competitionConverter.convertToBriefDTO(briefVO);
    }

    @Override
    @Transactional
    public CompetitionBriefDTO updateCompetition(Long id, UpdateCompetitionRequestDTO request) {
        if (!competitionDomainService.existsById(id)) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        competitionDomainService.updateCompetition(
                id,
                request.getName(),
                request.getShortName(),
                request.getLogoFileId(),
                request.getSummary(),
                request.getDetail(),
                request.getLevel(),
                request.getMonth(),
                request.getOrganizer());

        Optional<CompetitionVO> updated = competitionDomainService.getCompetitionById(id);
        if (updated.isEmpty()) {
            throw new GlobalException("更新竞赛失败");
        }

        CompetitionBriefVO briefVO = CompetitionBriefVO.builder()
                .id(updated.get().getId())
                .name(updated.get().getName())
                .shortName(updated.get().getShortName())
                .logoUrl(updated.get().getLogoUrl())
                .logoFileId(updated.get().getLogoFileId())
                .coverFileId(updated.get().getCoverFileId())
                .summary(updated.get().getSummary())
                .level(updated.get().getLevel())
                .month(updated.get().getMonth())
                .organizer(updated.get().getOrganizer())
                .build();
        return competitionConverter.convertToBriefDTO(briefVO);
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
