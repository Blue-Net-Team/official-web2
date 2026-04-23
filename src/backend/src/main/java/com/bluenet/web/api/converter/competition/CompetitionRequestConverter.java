package com.bluenet.web.api.converter.competition;

import com.bluenet.web.api.dto.competition.CompetitionRequestDTO;
import com.bluenet.web.api.dto.competition.MoveCompetitionRequestDTO;
import com.bluenet.web.api.dto.competition.UpdateSortOrderRequestDTO;
import com.bluenet.web.application.command.competition.CompetitionCommands;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import org.springframework.stereotype.Component;

/**
 * 竞赛请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class CompetitionRequestConverter {

    /**
     * 将创建/更新请求 DTO 转换为创建命令
     */
    public CompetitionCommands.CreateCompetitionCommand toCreateCommand(CompetitionRequestDTO dto) {
        return new CompetitionCommands.CreateCompetitionCommand(
                dto.getName(),
                dto.getShortName(),
                dto.getLogoFileId(),
                dto.getCoverFileId(),
                dto.getSummary(),
                parseLevel(dto.getLevel()),
                dto.getMonth(),
                dto.getOrganizer());
    }

    /**
     * 将创建/更新请求 DTO 转换为更新命令
     */
    public CompetitionCommands.UpdateCompetitionCommand toUpdateCommand(Long id, CompetitionRequestDTO dto) {
        return new CompetitionCommands.UpdateCompetitionCommand(
                id,
                dto.getName(),
                dto.getShortName(),
                dto.getLogoFileId(),
                dto.getCoverFileId(),
                dto.getSummary(),
                parseLevel(dto.getLevel()),
                dto.getMonth(),
                dto.getOrganizer());
    }

    /**
     * 将排序更新请求 DTO 转换为命令
     */
    public CompetitionCommands.UpdateSortOrderCommand toCommand(Long id, UpdateSortOrderRequestDTO dto) {
        return new CompetitionCommands.UpdateSortOrderCommand(id, dto.getSortOrder());
    }

    /**
     * 将移动请求 DTO 转换为命令
     */
    public CompetitionCommands.MoveCompetitionCommand toCommand(Long id, MoveCompetitionRequestDTO dto) {
        return new CompetitionCommands.MoveCompetitionCommand(id, dto.getDirection().toUpperCase());
    }

    private AwardLevel parseLevel(String level) {
        if (level == null) {
            return null;
        }
        return AwardLevel.valueOf(level.toUpperCase());
    }
}
