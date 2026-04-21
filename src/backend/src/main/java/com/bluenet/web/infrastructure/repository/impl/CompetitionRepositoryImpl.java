package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CompetitionRepositoryImpl implements CompetitionRepository {
    private final CompetitionMapper competitionMapper;
    private final FileMapper fileMapper;

    /**
     * 按展示排序查询指定数量的竞赛视图。
     *
     * @param limit
     *            最大返回数量。
     * @return 满足条件的竞赛 结果集合。
     */
    @Override
    public List<CompetitionVO> findCompetitionsWithLimit(int limit) {
        List<CompetitionDO> competitions = competitionMapper.selectCompetitionsWithLimit(limit);
        Map<Long, FileDO> files = loadLogoFiles(competitions);
        return competitions.stream()
                .map(competition -> toVO(competition, files.get(competition.getLogoFileId())))
                .toList();
    }

    /**
     * 按展示排序分页查询竞赛视图。
     *
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的竞赛 结果。
     */
    @Override
    public org.springframework.data.domain.Page<CompetitionVO> findCompetitionsPage(
            org.springframework.data.domain.Pageable pageable) {
        Page<CompetitionDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<CompetitionDO> result = competitionMapper.selectCompetitionsPage(page);
        Map<Long, FileDO> files = loadLogoFiles(result.getRecords());
        return new PageImpl<>(result.getRecords()
                .stream()
                .map(competition -> toVO(competition, files.get(competition.getLogoFileId())))
                .toList(), pageable, result.getTotal());
    }

    /**
     * 保存新的竞赛 记录。
     *
     * @param competition
     *            竞赛领域对象。
     * @return 新记录的主键。
     */
    @Override
    public Long save(Competition competition) {
        CompetitionDO dataObject = RepositoryObjectConverter.copy(competition, CompetitionDO.class);
        RepositoryObjectConverter.insert(competitionMapper, dataObject, CompetitionDO.class);
        RepositoryObjectConverter.copyInto(dataObject, competition);
        return dataObject.getId();
    }

    /**
     * 更新已有竞赛 记录。
     *
     * @param competition
     *            竞赛领域对象。
     */
    @Override
    public void update(Competition competition) {
        RepositoryObjectConverter.updateById(
                competitionMapper,
                RepositoryObjectConverter.copy(competition, CompetitionDO.class),
                CompetitionDO.class);
    }

    /**
     * 删除指定竞赛 记录。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    public void deleteById(Long id) {
        competitionMapper.deleteById(id);
    }

    /**
     * 判断是否存在满足条件的竞赛 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsById(Long id) {
        return competitionMapper.selectById(id) != null;
    }

    /**
     * 查询符合条件的竞赛 记录。
     *
     * @return 转换后的目标模型对象。
     */
    @Override
    public Integer findMaxSortOrder() {
        return competitionMapper.selectMaxSortOrder();
    }

    /**
     * 批量更新竞赛 展示排序值。
     *
     * @param sortItems
     *            需要更新排序的条目集合。
     */
    @Override
    public void batchUpdateSortOrder(List<com.bluenet.web.domain.repository.CompetitionRepository.SortItem> sortItems) {
        sortItems.forEach(item -> competitionMapper.updateSortOrderById(item.id(), item.sortOrder()));
    }

    /**
     * 按主键查询竞赛 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询或处理得到的竞赛 结果。
     */
    @Override
    public com.bluenet.web.domain.model.entity.Competition findById(Long id) {
        return RepositoryObjectConverter.copy(competitionMapper.selectById(id), Competition.class);
    }

    /**
     * 查询当前竞赛相邻位置的竞赛记录，用于排序调整。
     *
     * @param sortOrder
     *            展示排序值。
     * @param direction
     *            技术方向过滤条件。
     * @return 查询或处理得到的竞赛 结果。
     */
    @Override
    public com.bluenet.web.domain.model.entity.Competition findAdjacent(Integer sortOrder, String direction) {
        if ("UP".equals(direction)) {
            return RepositoryObjectConverter.copy(competitionMapper.selectAdjacentUp(sortOrder), Competition.class);
        } else {
            return RepositoryObjectConverter.copy(competitionMapper.selectAdjacentDown(sortOrder), Competition.class);
        }
    }

    /**
     * 批量加载竞赛 关联的文件数据，用于仓储层组装展示视图。
     *
     * @param competitions
     *            竞赛数据行集合。
     * @return 查询或处理得到的竞赛 结果。
     */
    private Map<Long, FileDO> loadLogoFiles(List<CompetitionDO> competitions) {
        List<Long> logoFileIds = competitions.stream()
                .map(CompetitionDO::getLogoFileId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // 空 Map 需要允许 get(null) 返回 null，兼容 logoFileId 为空的竞赛记录。
        return logoFileIds.isEmpty()
                ? Collections.emptyMap()
                : fileMapper.selectBatchIds(logoFileIds)
                        .stream()
                        .collect(Collectors.toMap(FileDO::getId, Function.identity()));
    }

    /**
     * 将竞赛 及其关联数据组装为领域视图对象。
     *
     * @param competition
     *            竞赛领域对象。
     * @param logoFile
     *            竞赛 Logo 文件数据行。
     * @return 转换后的目标模型对象。
     */
    private CompetitionVO toVO(CompetitionDO competition, FileDO logoFile) {
        return CompetitionVO.builder()
                .id(competition.getId())
                .name(competition.getName())
                .shortName(competition.getShortName())
                .logoFileId(competition.getLogoFileId())
                .coverFileId(competition.getCoverFileId())
                .logoUrl(logoFile == null ? null : logoFile.getUrl())
                .summary(competition.getSummary())
                .level(competition.getLevel() == null ? null : competition.getLevel().getValue())
                .month(competition.getMonth())
                .organizer(competition.getOrganizer())
                .sortOrder(competition.getSortOrder())
                .build();
    }
}
