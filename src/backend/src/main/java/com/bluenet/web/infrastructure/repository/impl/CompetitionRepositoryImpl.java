package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CompetitionRepositoryImpl implements CompetitionRepository {
    private final CompetitionMapper competitionMapper;

    @Override
    public List<CompetitionBriefVO> findEnabledCompetitionsWithLimit(int limit) {
        return competitionMapper.selectEnabledCompetitionsWithLimit(limit);
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
