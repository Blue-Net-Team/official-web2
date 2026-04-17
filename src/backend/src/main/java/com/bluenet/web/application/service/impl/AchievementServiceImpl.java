package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.api.dto.achievement.CreateAchievementRequestDTO;
import com.bluenet.web.api.dto.achievement.UpdateAchievementRequestDTO;
import com.bluenet.web.application.converter.AchievementConverter;
import com.bluenet.web.application.service.AchievementService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.domain.service.AchievementDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {
    private final AchievementRepository achievementRepository;
    private final AchievementConverter achievementConverter;
    private final AchievementDomainService achievementDomainService;
    private final FileDomainService fileDomainService;

    @Override
    public PageDTO<AchievementDTO> getAchievements(Integer page, Integer size, AchievementType type,
            AwardLevel awardLevel,
            Integer year) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 12;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<AchievementVO> voPage = achievementRepository.findAchievementsWithFilter(type, awardLevel, year, pageable);
        Page<AchievementDTO> dtoPage = achievementConverter.convertToDTOPage(voPage);
        return PageDTO.from(dtoPage);
    }

    @Override
    public AchievementStatsDTO getAchievementStats() {
        AchievementStatsVO vo = achievementRepository.findAchievementStats();
        return achievementConverter.convertToStatsDTO(vo);
    }

    @Override
    @Transactional
    public AchievementDTO createAchievement(CreateAchievementRequestDTO request) {
        validateFile(request.getFileId());

        Achievement achievement = achievementDomainService.createAchievement(
                request.getTitle(),
                request.getType(),
                request.getRelateTo(),
                request.getAchieveAt(),
                request.getAwardLevel(),
                request.getAwardName(),
                request.getFileId());

        AchievementVO savedVO = achievementRepository.save(achievement);
        return achievementConverter.toDTO(savedVO);
    }

    @Override
    @Transactional
    public AchievementDTO updateAchievement(Long id, UpdateAchievementRequestDTO request) {
        validateFile(request.getFileId());

        AchievementVO existingVO = achievementRepository.findById(id);
        if (existingVO == null) {
            throw new DataNotFound("成就不存在");
        }

        Achievement existing = new Achievement();
        existing.setId(id);
        existing.setTitle(existingVO.getTitle());
        existing.setType(existingVO.getType());
        existing.setRelateTo(existingVO.getRelateTo());
        existing.setAchieveAt(existingVO.getAchieveAt());
        existing.setAwardLevel(existingVO.getAwardLevel());
        existing.setAwardName(existingVO.getAwardName());
        existing.setFileId(existingVO.getFileId());

        Achievement updated = achievementDomainService.updateAchievement(
                existing,
                request.getTitle(),
                request.getType(),
                request.getRelateTo(),
                request.getAchieveAt(),
                request.getAwardLevel(),
                request.getAwardName(),
                request.getFileId());

        AchievementVO savedVO = achievementRepository.update(updated);
        return achievementConverter.toDTO(savedVO);
    }

    @Override
    @Transactional
    public void deleteAchievement(Long id) {
        AchievementVO existingVO = achievementRepository.findById(id);
        if (existingVO == null) {
            throw new DataNotFound("成就不存在");
        }
        achievementRepository.delete(id);
    }

    private void validateFile(Long fileId) {
        FileVO file = fileDomainService.getFileById(fileId);
        if (file == null) {
            throw new DataNotFound("文件不存在");
        }
        if (file.getType() != FileType.NORMAL_IMG) {
            throw new BadRequest("文件类型不匹配，期望 NORMAL_IMG");
        }
    }
}
