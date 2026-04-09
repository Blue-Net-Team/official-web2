package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.api.dto.member.DirectionLeaderDTO;
import com.bluenet.web.api.dto.member.MemberBriefDTO;
import com.bluenet.web.api.dto.member.MemberDetailDTO;
import com.bluenet.web.api.dto.member.MemberListQueryDTO;
import com.bluenet.web.application.converter.MemberConverter;
import com.bluenet.web.application.service.MemberService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.domain.model.vo.MemberVO;
import com.bluenet.web.domain.service.MemberDomainService;
import com.bluenet.web.domain.service.UserExperienceDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberDomainService memberDomainService;
    private final MemberConverter memberConverter;
    private final UserExperienceDomainService userExperienceDomainService;

    private static final int MAX_PAGE_SIZE = 100;

    @Override
    public PageDTO<MemberBriefDTO> getMemberList(MemberListQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : 0;
        int size = query.getSize() != null ? Math.min(query.getSize(), MAX_PAGE_SIZE) : 20;

        Pageable pageable = PageRequest.of(page, size);
        Page<MemberVO> memberPage = memberDomainService.getMemberList(query.getDirection(), pageable);

        Page<MemberBriefDTO> dtoPage = memberPage.map(memberConverter::toBriefDTO);
        return PageDTO.from(dtoPage);
    }

    @Override
    public MemberDetailDTO getMemberById(Long id) {
        MemberVO member = memberDomainService.getMemberById(id)
                .orElseThrow(() -> new DataNotFound("成员不存在"));

        return memberConverter.toDetailDTO(member);
    }

    @Override
    public List<DirectionLeaderDTO> getDirectionLeaders() {
        List<MemberVO> leaders = memberDomainService.getDirectionLeaders();
        return memberConverter.toDirectionLeaderDTOs(leaders);
    }

    @Override
    public List<ExperienceDTO> getMemberExperiences(Long memberId, String type) {
        MemberVO member = memberDomainService.getMemberById(memberId)
                .orElseThrow(() -> new DataNotFound("成员不存在"));

        if (member.getRole() == null || !isTeamMember(member.getRole())) {
            log.info("成员 {} 不是团队成员，返回空经历列表", memberId);
            return Collections.emptyList();
        }

        List<ExperienceVO> experiences;
        if (type != null && !type.isBlank()) {
            ExperienceType experienceType = parseExperienceType(type);
            experiences = userExperienceDomainService.getExperiencesByType(memberId, experienceType);
        } else {
            experiences = userExperienceDomainService.getExperiences(memberId);
        }

        return experiences.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private boolean isTeamMember(RoleType roleType) {
        return roleType.isAtLeast(RoleType.MEMBER);
    }

    private ExperienceType parseExperienceType(String type) {
        try {
            return ExperienceType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequest("无效的经历类型: " + type);
        }
    }

    private ExperienceDTO convertToDTO(ExperienceVO vo) {
        ExperienceDTO dto = ExperienceDTO.builder()
                .id(String.valueOf(vo.getId()))
                .type(vo.getType().getValue())
                .startDate(vo.getStartTime())
                .endDate(vo.getEndTime())
                .build();

        dto.setNameByType(vo.getType().getValue(), vo.getTitle());

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            switch (vo.getType()) {
                case PROJECT -> {
                    com.bluenet.web.domain.model.vo.content.ProjectContent content = objectMapper
                            .readValue(vo.getContent(), com.bluenet.web.domain.model.vo.content.ProjectContent.class);
                    dto.setRole(content.getRole());
                    dto.setDescription(content.getDescription());
                    dto.setTechStack(content.getTechStack());
                    dto.setDemoUrl(content.getDemoUrl());
                }
                case COMPETITION -> {
                    com.bluenet.web.domain.model.vo.content.CompetitionContent content = objectMapper.readValue(
                            vo.getContent(),
                            com.bluenet.web.domain.model.vo.content.CompetitionContent.class);
                    dto.setRole(content.getRole());
                    dto.setDate(content.getDate());
                    dto.setLevel(content.getLevel());
                    dto.setAward(content.getAward());
                    dto.setTeamSize(content.getTeamSize());
                    dto.setDescription(content.getDescription());
                    dto.setCertificateUrl(content.getCertificateUrl());
                }
                case INTERNSHIP -> {
                    com.bluenet.web.domain.model.vo.content.InternshipContent content = objectMapper.readValue(
                            vo.getContent(),
                            com.bluenet.web.domain.model.vo.content.InternshipContent.class);
                    dto.setPosition(content.getPosition());
                    dto.setDescription(content.getDescription());
                    dto.setAchievements(content.getAchievements());
                    dto.setStatus(content.getStatus());
                }
            }
        } catch (Exception e) {
            log.error("解析经历内容失败: id={}", vo.getId(), e);
        }

        return dto;
    }
}
