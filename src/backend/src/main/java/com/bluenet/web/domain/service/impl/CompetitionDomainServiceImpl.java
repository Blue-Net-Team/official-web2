package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.domain.service.CompetitionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionDomainServiceImpl implements CompetitionDomainService {
    private final CompetitionRepository competitionRepository;

    @Override
    public List<CompetitionBriefVO> getCompetitionList(int limit) {
        return competitionRepository.findEnabledCompetitionsWithLimit(limit);
    }

    @Override
    public Long createCompetition(String name, String shortName, Long logoFileId, String summary, String level,
            String month, String organizer) {
        Competition competition = new Competition();
        competition.setName(name);
        competition.setShortName(shortName);
        competition.setLogoFileId(logoFileId);
        competition.setSummary(summary);
        competition.setLevel(level != null ? level : "省级");
        competition.setMonth(month);
        competition.setOrganizer(organizer);
        competition.setSortOrder(0);
        return competitionRepository.save(competition);
    }

    @Override
    public void updateCompetition(Long id, String name, String shortName, Long logoFileId, String summary,
            String level, String month, String organizer) {
        Competition competition = new Competition();
        competition.setId(id);
        competition.setName(name);
        competition.setShortName(shortName);
        competition.setLogoFileId(logoFileId);
        competition.setSummary(summary);
        competition.setLevel(level);
        competition.setMonth(month);
        competition.setOrganizer(organizer);
        competitionRepository.update(competition);
    }

    @Override
    public void deleteCompetition(Long id) {
        competitionRepository.deleteById(id);
    }

    @Override
    public void updateSortOrder(Long id, Integer sortOrder) {
        Competition competition = new Competition();
        competition.setId(id);
        competition.setSortOrder(sortOrder);
        competitionRepository.update(competition);
    }

    @Override
    public boolean existsById(Long id) {
        return competitionRepository.existsById(id);
    }

    @Override
    public void updateLogo(Long id, Long logoFileId) {
        Competition competition = new Competition();
        competition.setId(id);
        competition.setLogoFileId(logoFileId);
        competitionRepository.update(competition);
    }

    @Override
    public void updateCover(Long id, Long coverFileId) {
        Competition competition = new Competition();
        competition.setId(id);
        competition.setCoverFileId(coverFileId);
        competitionRepository.update(competition);
    }
}
