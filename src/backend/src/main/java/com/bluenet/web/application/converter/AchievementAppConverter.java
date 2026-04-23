package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.application.AchievementResult;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 成就应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class AchievementAppConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
    public AchievementDTO toDTO(AchievementResult result) {
        if (result == null) {
            return null;
        }
        return AchievementDTO.builder()
                .id(result.id())
                .title(result.title())
                .relateTo(result.relateTo())
                .type(result.type())
                .achieveAt(result.achieveAt())
                .awardLevel(result.awardLevel())
                .awardLevelName(result.awardLevelName())
                .awardName(result.awardName())
                .competitionName(result.competitionName())
                .competitionShortName(result.competitionShortName())
                .competitionLogoFileId(result.competitionLogoFileId())
                .fileId(result.fileId())
                .fileUrl(result.fileUrl())
                .build();
    }

    /**
     * 将应用层结果列表转换为 API 响应 DTO 列表
     */
    public List<AchievementDTO> toDTOList(List<AchievementResult> results) {
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 将统计 VO 转换为统计 DTO
     */
    public AchievementStatsDTO toStatsDTO(AchievementStatsVO vo) {
        if (vo == null) {
            return null;
        }
        return AchievementStatsDTO.builder()
                .totalAchievements(vo.getTotalAchievements())
                .nationalCount(vo.getNationalCount())
                .provincialCount(vo.getProvincialCount())
                .schoolCount(vo.getSchoolCount())
                .build();
    }
}
