package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.application.converter.AchievementConverter;
import com.bluenet.web.application.service.AchievementService;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import com.bluenet.web.domain.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {
    private final AchievementRepository achievementRepository;
    private final AchievementConverter achievementConverter;

    @Override
    public PageDTO<AchievementDTO> getAchievements(Integer page, Integer size, String type, String awardLevel,
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
}
