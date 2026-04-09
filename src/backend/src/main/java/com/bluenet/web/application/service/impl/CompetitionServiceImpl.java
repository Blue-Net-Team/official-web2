package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.competition.*;
import com.bluenet.web.application.converter.CompetitionConverter;
import com.bluenet.web.application.service.CompetitionService;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import com.bluenet.web.domain.service.CompetitionDomainService;
import com.bluenet.web.domain.service.IntroduceImageDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {
    private static final int MAX_IMAGES_PER_COMPETITION = 20;

    private final CompetitionDomainService competitionDomainService;
    private final IntroduceImageDomainService introduceImageDomainService;
    private final CompetitionConverter competitionConverter;

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

        // 为每个竞赛查询第一张介绍图片
        for (CompetitionBriefVO vo : voList) {
            List<IntroduceImageVO> images = introduceImageDomainService.getCompetitionImages(vo.getId());
            if (!images.isEmpty()) {
                // 按 sort_order 排序取第一张
                images.sort((a, b) -> {
                    int sortA = a.getSortOrder() != null ? a.getSortOrder() : 0;
                    int sortB = b.getSortOrder() != null ? b.getSortOrder() : 0;
                    return Integer.compare(sortB, sortA); // 降序，sort_order 越大越靠前
                });
                vo.setIntroduceImageFileId(images.get(0).getFileId());
            }
        }

        return competitionConverter.convertToResponseDTOList(voList);
    }

    @Override
    public CompetitionDetailDTO getCompetitionDetail(Long id) {
        Optional<CompetitionVO> competitionOpt = competitionDomainService.getCompetitionById(id);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        List<IntroduceImageVO> images = introduceImageDomainService.getCompetitionImages(id);
        return competitionConverter.convertToDetailDTO(competitionOpt.get(), images);
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

        List<IntroduceImageVO> images = introduceImageDomainService.getCompetitionImages(id);
        CompetitionBriefVO briefVO = CompetitionBriefVO.builder()
                .id(created.get().getId())
                .name(created.get().getName())
                .shortName(created.get().getShortName())
                .logoUrl(created.get().getLogoUrl())
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
                request.getOrganizer(),
                request.getEnabled());

        Optional<CompetitionVO> updated = competitionDomainService.getCompetitionById(id);
        if (updated.isEmpty()) {
            throw new GlobalException("更新竞赛失败");
        }

        CompetitionBriefVO briefVO = CompetitionBriefVO.builder()
                .id(updated.get().getId())
                .name(updated.get().getName())
                .shortName(updated.get().getShortName())
                .logoUrl(updated.get().getLogoUrl())
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

        List<IntroduceImageVO> images = introduceImageDomainService.getCompetitionImages(id);
        for (IntroduceImageVO image : images) {
            introduceImageDomainService.removeCompetitionImage(image.getId());
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
    public CompetitionImageDTO addCompetitionImage(Long id, AddCompetitionImageRequestDTO request) {
        if (!competitionDomainService.existsById(id)) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        int currentCount = introduceImageDomainService.countCompetitionImages(id);
        if (currentCount >= MAX_IMAGES_PER_COMPETITION) {
            throw new IllegalArgumentException("每个竞赛最多关联20张照片");
        }

        Long imageId = introduceImageDomainService.addCompetitionImage(
                id,
                request.getFileId(),
                request.getDescription());

        return CompetitionImageDTO.builder()
                .id(imageId)
                .description(request.getDescription())
                .build();
    }

    @Override
    @Transactional
    public void removeCompetitionImage(Long id, Long imageId) {
        if (!competitionDomainService.existsById(id)) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        if (!introduceImageDomainService.existsById(imageId)) {
            throw new IllegalArgumentException("图片不存在");
        }

        introduceImageDomainService.removeCompetitionImage(imageId);
    }
}
