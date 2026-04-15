package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CompetitionRepositoryImpl implements CompetitionRepository {
    private final CompetitionMapper competitionMapper;

    @Override
    public List<CompetitionVO> findCompetitionsWithLimit(int limit) {
        return competitionMapper.selectCompetitionsWithLimit(limit);
    }

    @Override
    public org.springframework.data.domain.Page<CompetitionVO> findCompetitionsPage(
            org.springframework.data.domain.Pageable pageable) {
        Page<CompetitionVO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<CompetitionVO> result = competitionMapper.selectCompetitionsPage(page);
        return new PageImpl<>(result.getRecords(), pageable, result.getTotal());
    }

    @Override
    public Long save(Competition competition) {
        competitionMapper.insert(competition);
        return competition.getId();
    }

    @Override
    public void update(Competition competition) {
        competitionMapper.updateById(competition);
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
    public void batchUpdateSortOrder(List<com.bluenet.web.domain.repository.CompetitionRepository.SortItem> sortItems) {
        sortItems.forEach(item -> competitionMapper.updateSortOrderById(item.id(), item.sortOrder()));
    }

    @Override
    public com.bluenet.web.domain.model.entity.Competition findById(Long id) {
        return competitionMapper.selectById(id);
    }

    @Override
    public com.bluenet.web.domain.model.entity.Competition findAdjacent(Integer sortOrder, String direction) {
        if ("UP".equals(direction)) {
            return competitionMapper.selectAdjacentUp(sortOrder);
        } else {
            return competitionMapper.selectAdjacentDown(sortOrder);
        }
    }
}
