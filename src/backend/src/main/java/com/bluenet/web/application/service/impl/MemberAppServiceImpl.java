package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.application.MemberResult;
import com.bluenet.web.application.command.member.MemberCommands;
import com.bluenet.web.application.service.MemberAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Member;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.domain.repository.MemberRepository;
import com.bluenet.web.domain.service.UserExperienceDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 成员应用服务实现。
 * <p>
 * 实现成员聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberAppServiceImpl implements MemberAppService {
    private final MemberRepository memberRepository;
    private final UserExperienceDomainService userExperienceDomainService;

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 查询成员列表。
     *
     * @param command
     *            查询成员列表命令
     * @return 成员分页结果
     */
    @Override
    public Page<MemberResult> getMemberList(MemberCommands.GetMemberListCommand command) {
        int page = command.page() != null ? command.page() : 0;
        int size = command.size() != null ? Math.min(command.size(), MAX_PAGE_SIZE) : 20;

        Pageable pageable = PageRequest.of(page, size);
        Page<Member> memberPage = memberRepository.findAll(command.direction(), pageable);

        return memberPage.map(this::toResult);
    }

    /**
     * 根据ID查询成员。
     *
     * @param id
     *            成员ID
     * @return 成员结果
     */
    @Override
    public MemberResult getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("成员不存在"));
        return toResult(member);
    }

    /**
     * 查询方向负责人。
     *
     * @return 方向负责人列表
     */
    @Override
    public List<MemberResult> getDirectionLeaders() {
        return memberRepository.findDirectionLeaders()
                .stream()
                .map(this::toResult)
                .toList();
    }

    /**
     * 查询成员经历。
     *
     * @param memberId
     *            成员ID
     * @param type
     *            经历类型
     * @return 经历DTO列表
     */
    @Override
    public List<ExperienceDTO> getMemberExperiences(Long memberId, String type) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFound("成员不存在"));

        if (!member.isTeamMember()) {
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

    private ExperienceType parseExperienceType(String type) {
        try {
            return ExperienceType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequest("无效的经历类型: " + type);
        }
    }

    private MemberResult toResult(Member member) {
        return new MemberResult(
                member.getId(),
                member.getStudentId(),
                member.getUsername(),
                member.getNickname(),
                member.getDirection(),
                member.getJob(),
                member.getAvatarFileId(),
                member.getCollege(),
                member.getMajor(),
                member.getGender(),
                member.getRole(),
                member.getRoleName(),
                member.getBio(),
                member.getGithubUsername(),
                member.getWechatQrcode(),
                member.getEnrollmentYear(),
                member.getAssessmentGradeYear());
    }

    private ExperienceDTO convertToDTO(ExperienceVO vo) {
        ExperienceDTO dto = ExperienceDTO.builder()
                .id(String.valueOf(vo.getId()))
                .type(vo.getType().name())
                .startDate(vo.getStartTime())
                .endDate(vo.getEndTime())
                .build();

        dto.setNameByType(vo.getType().name(), vo.getTitle());

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
