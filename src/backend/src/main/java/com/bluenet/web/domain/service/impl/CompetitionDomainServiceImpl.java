package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.CompetitionVO;
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
    public List<CompetitionVO> getCompetitionList(int limit) {
        return competitionRepository.findCompetitionsWithLimit(limit);
    }

    @Override
    public org.springframework.data.domain.Page<CompetitionVO> getCompetitionPage(
            org.springframework.data.domain.Pageable pageable) {
        return competitionRepository.findCompetitionsPage(pageable);
    }

    @Override
    public Long createCompetition(String name, String shortName, Long logoFileId, Long coverFileId, String summary,
            AwardLevel level, String month, String organizer) {
        Competition competition = new Competition();
        competition.setName(name);
        competition.setShortName(shortName);
        competition.setLogoFileId(logoFileId);
        competition.setCoverFileId(coverFileId);
        competition.setSummary(summary);
        competition.setLevel(level != null ? level : AwardLevel.PROVINCIAL);
        competition.setMonth(month);
        competition.setOrganizer(organizer);
        competition.setSortOrder(0);
        return competitionRepository.save(competition);
    }

    @Override
    public void updateCompetition(Long id, String name, String shortName, Long logoFileId, Long coverFileId,
            String summary, AwardLevel level, String month, String organizer) {
        Competition competition = new Competition();
        competition.setId(id);
        competition.setName(name);
        competition.setShortName(shortName);
        competition.setLogoFileId(logoFileId);
        competition.setCoverFileId(coverFileId);
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
}
