package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.readmodel.CompetitionReadModel;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.infrastructure.repository.converter.CompetitionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.CompetitionDO;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CompetitionRepositoryImpl implements CompetitionRepository {
    private final CompetitionMapper competitionMapper;
    private final FileMapper fileMapper;
    private final CompetitionRepositoryConverter converter;

    @Override
    public List<CompetitionReadModel> findCompetitionsWithLimit(int limit) {
        List<CompetitionDO> competitions = competitionMapper.selectCompetitionsWithLimit(limit);
        Map<Long, FileDO> files = loadLogoFiles(competitions);
        return competitions.stream()
                .map(competition -> toVO(competition, files.get(competition.getLogoFileId())))
                .toList();
    }

    @Override
    public org.springframework.data.domain.Page<CompetitionReadModel> findCompetitionsPage(
            org.springframework.data.domain.Pageable pageable) {
        Page<CompetitionDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<CompetitionDO> result = competitionMapper.selectCompetitionsPage(page);
        Map<Long, FileDO> files = loadLogoFiles(result.getRecords());
        return new PageImpl<>(result.getRecords()
                .stream()
                .map(competition -> toVO(competition, files.get(competition.getLogoFileId())))
                .toList(), pageable, result.getTotal());
    }

    @Override
    public void save(Competition competition) {
        CompetitionDO dataObject = converter.toDataObject(competition);
        if (dataObject.getId() == null) {
            competitionMapper.insert(dataObject);
            competition.setId(dataObject.getId());
        } else {
            competitionMapper.updateById(dataObject);
        }
    }
    @Override
    public void deleteById(Long id) {
        competitionMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return competitionMapper.selectById(id) != null;
    }

    @Override
    public Integer findMaxSortOrder() {
        return competitionMapper.selectMaxSortOrder();
    }

    @Override
    public void batchUpdateSortOrder(List<CompetitionRepository.SortItem> sortItems) {
        if (sortItems.isEmpty()) {
            return;
        }
        competitionMapper.batchUpdateSortOrder(sortItems);
    }

    @Override
    public Optional<Competition> findById(Long id) {
        CompetitionDO dataObject = competitionMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public Optional<Competition> findAdjacent(Integer sortOrder, String direction) {
        CompetitionDO dataObject;
        if ("UP".equals(direction)) {
            dataObject = competitionMapper.selectAdjacentUp(sortOrder);
        } else {
            dataObject = competitionMapper.selectAdjacentDown(sortOrder);
        }
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    private Map<Long, FileDO> loadLogoFiles(List<CompetitionDO> competitions) {
        List<Long> logoFileIds = competitions.stream()
                .map(CompetitionDO::getLogoFileId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return logoFileIds.isEmpty()
                ? Collections.emptyMap()
                : fileMapper.selectBatchIds(logoFileIds)
                        .stream()
                        .collect(Collectors.toMap(FileDO::getId, Function.identity()));
    }

    private CompetitionReadModel toVO(CompetitionDO competition, FileDO logoFile) {
        return CompetitionReadModel.builder()
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
