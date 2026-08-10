package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.competition.CompetitionResult;
import com.bluenet.web.application.command.competition.CompetitionCommands;
import com.bluenet.web.application.service.CompetitionAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.readmodel.CompetitionReadModel;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.domain.service.FileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 竞赛应用服务实现。
 * <p>
 * 实现竞赛聚合在应用层的业务逻辑编排。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionAppServiceImpl implements CompetitionAppService {
    private final CompetitionRepository competitionRepository;
    private final FileDomainService fileDomainService;

    /**
     * 查询竞赛响应列表。
     *
     * @param limit
     *            限制数量
     * @return 竞赛VO列表
     */
    @Override
    public List<CompetitionReadModel> getCompetitionResponseList(int limit) {
        int validLimit = Math.min(Math.max(limit, 1), 50);
        return competitionRepository.findCompetitionsWithLimit(validLimit);
    }

    /**
     * 分页查询竞赛列表。
     *
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @return 竞赛分页结果
     */
    @Override
    public Page<CompetitionReadModel> getCompetitionPage(Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        pageSize = Math.min(Math.max(pageSize, 1), 50);

        return competitionRepository.findCompetitionsPage(PageRequest.of(pageNum, pageSize));
    }

    /**
     * 创建竞赛。
     *
     * @param command
     *            创建竞赛命令
     * @return 创建后的竞赛结果
     */
    @Override
    @Transactional
    public CompetitionResult createCompetition(CompetitionCommands.CreateCompetitionCommand command) {
        validateFileId(command.logoFileId(), "Logo");
        validateFileId(command.coverFileId(), "封面");

        if (competitionRepository.existsByName(command.name())) {
            throw new BadRequest("竞赛名称已存在");
        }

        Integer maxSortOrder = competitionRepository.findMaxSortOrder();
        Integer sortOrder = maxSortOrder != null ? maxSortOrder + 1 : 1;

        Competition competition = Competition.create(
                command.name(),
                command.shortName(),
                command.logoFileId(),
                command.coverFileId(),
                command.summary(),
                command.level(),
                command.month(),
                command.organizer(),
                sortOrder);

        competitionRepository.save(competition);
        return toResult(competition);
    }

    /**
     * 更新竞赛。
     *
     * @param command
     *            更新竞赛命令
     * @return 更新后的竞赛结果
     */
    @Override
    @Transactional
    public CompetitionResult updateCompetition(CompetitionCommands.UpdateCompetitionCommand command) {
        Competition competition = competitionRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("竞赛不存在"));

        validateFileId(command.logoFileId(), "Logo");
        validateFileId(command.coverFileId(), "封面");

        if (!competition.getName().equals(command.name())
                && competitionRepository.existsByName(command.name())) {
            throw new BadRequest("竞赛名称已存在");
        }

        competition.update(
                command.name(),
                command.shortName(),
                command.logoFileId(),
                command.coverFileId(),
                command.summary(),
                command.level(),
                command.month(),
                command.organizer());

        competitionRepository.save(competition);
        return toResult(competition);
    }

    /**
     * 删除竞赛。
     *
     * @param id
     *            竞赛ID
     */
    @Override
    @Transactional
    public void deleteCompetition(Long id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("竞赛不存在"));
        competitionRepository.deleteById(id);
    }

    /**
     * 更新排序号。
     *
     * @param command
     *            更新排序号命令
     */
    @Override
    @Transactional
    public void updateSortOrder(CompetitionCommands.UpdateSortOrderCommand command) {
        Competition competition = competitionRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("竞赛不存在"));
        competition.updateSortOrder(command.sortOrder());
        competitionRepository.save(competition);
    }

    /**
     * 批量更新排序号。
     *
     * @param command
     *            批量排序命令
     */
    @Override
    @Transactional
    public void batchUpdateSortOrder(CompetitionCommands.BatchUpdateSortOrderCommand command) {
        List<CompetitionRepository.SortItem> sortItems = command.items()
                .stream()
                .map(item -> new CompetitionRepository.SortItem(item.id(), item.sortOrder()))
                .toList();
        sortItems.forEach(item -> {
            if (!competitionRepository.existsById(item.id())) {
                throw new IllegalArgumentException("竞赛不存在: " + item.id());
            }
        });
        competitionRepository.batchUpdateSortOrder(sortItems);
    }

    /**
     * 移动竞赛位置。
     *
     * @param command
     *            移动竞赛命令
     */
    @Override
    @Transactional
    public void moveCompetition(CompetitionCommands.MoveCompetitionCommand command) {
        String direction = command.direction();
        if (!"UP".equals(direction) && !"DOWN".equals(direction)) {
            throw new IllegalArgumentException("移动方向必须是 UP 或 DOWN");
        }

        Competition competition = competitionRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("竞赛不存在"));

        Integer currentSortOrder = competition.getSortOrder();
        if (currentSortOrder == null) {
            throw new IllegalArgumentException("竞赛排序号缺失");
        }

        Competition adjacent = competitionRepository.findAdjacent(currentSortOrder, direction)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "UP".equals(direction) ? "已是第一个" : "已是最后一个"));

        Integer tempSortOrder = currentSortOrder;
        competition.updateSortOrder(adjacent.getSortOrder());
        adjacent.updateSortOrder(tempSortOrder);
        competitionRepository.save(competition);
        competitionRepository.save(adjacent);
    }

    private void validateFileId(Long fileId, String fieldName) {
        if (fileId == null) {
            return;
        }
        File file;
        try {
            file = fileDomainService.getFileById(fileId);
        } catch (DataNotFound e) {
            throw new DataNotFound(fieldName + "文件不存在");
        }
        if (file.getType() != FileType.NORMAL_IMG) {
            throw new BadRequest(fieldName + "文件类型不匹配，期望 NORMAL_IMG");
        }
    }

    private CompetitionResult toResult(Competition competition) {
        return new CompetitionResult(
                competition.getId(),
                competition.getName(),
                competition.getShortName(),
                competition.getLevel() != null ? competition.getLevel().getValue() : null,
                competition.getMonth(),
                competition.getOrganizer(),
                competition.getSummary(),
                competition.getLogoFileId(),
                competition.getCoverFileId(),
                competition.getSortOrder());
    }
}
