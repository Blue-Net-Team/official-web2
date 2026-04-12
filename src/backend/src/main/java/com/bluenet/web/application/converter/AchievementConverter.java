package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AchievementConverter {
    public AchievementDTO convertToDTO(AchievementVO vo) {
        return AchievementDTO.builder()
                .id(vo.getId())
                .title(vo.getTitle())
                .relateTo(vo.getRelateTo())
                .type(vo.getType() != null ? vo.getType().name() : null)
                .achieveAt(vo.getAchieveAt())
                .awardLevel(vo.getAwardLevel() != null ? vo.getAwardLevel().name() : null)
                .awardLevelName(vo.getAwardLevel() != null ? vo.getAwardLevel().getDescription() : null)
                .awardName(vo.getAwardName())
                .competitionName(vo.getCompetitionName())
                .competitionShortName(vo.getCompetitionShortName())
                .competitionLogoFileId(vo.getCompetitionLogoFileId())
                .fileId(vo.getFileId())
                .fileUrl(vo.getFileUrl())
                .build();
    }

    public List<AchievementDTO> convertToDTOList(List<AchievementVO> voList) {
        return voList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<AchievementDTO> convertToDTOPage(Page<AchievementVO> voPage) {
        return voPage.map(this::convertToDTO);
    }

    public AchievementStatsDTO convertToStatsDTO(AchievementStatsVO vo) {
        return AchievementStatsDTO.builder()
                .totalAchievements(vo.getTotalAchievements())
                .nationalCount(vo.getNationalCount())
                .provincialCount(vo.getProvincialCount())
                .schoolCount(vo.getSchoolCount())
                .build();
    }
}
