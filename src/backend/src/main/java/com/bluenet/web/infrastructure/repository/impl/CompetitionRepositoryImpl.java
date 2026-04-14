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

import java.time.LocalDateTime;
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
        competition.setCreatedAt(LocalDateTime.now());
        competition.setUpdatedAt(LocalDateTime.now());
        competitionMapper.insert(competition);
        return competition.getId();
    }

    @Override
    public void update(Competition competition) {
        competition.setUpdatedAt(LocalDateTime.now());
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
}
