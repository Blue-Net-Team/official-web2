package com.bluenet.web.api.converter.achievement;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.application.result.achievement.AchievementResult;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 成就响应转换器
 * <p>
 * 负责将应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class AchievementResponseConverter {

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
     * 将应用层结果分页转换为 API 响应 DTO 分页
     */
    public PageDTO<AchievementDTO> toDTOPage(Page<AchievementResult> resultPage) {
        if (resultPage == null) {
            return null;
        }
        Page<AchievementDTO> dtoPage = resultPage.map(this::toDTO);
        return PageDTO.from(dtoPage);
    }

    /**
     * 将统计 VO 转换为统计 DTO
     */
    public AchievementStatsDTO toStatsDTO(AchievementStatistics vo) {
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
