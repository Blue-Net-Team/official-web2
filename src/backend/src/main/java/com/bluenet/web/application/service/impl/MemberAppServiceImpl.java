package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.MemberResult;
import com.bluenet.web.application.UserExperienceResult;
import com.bluenet.web.application.command.member.MemberCommands;
import com.bluenet.web.application.service.MemberAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Member;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.repository.MemberRepository;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final UserExperienceRepository userExperienceRepository;

    private static final int MAX_PAGE_SIZE = 100;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM");

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
     * @return 经历结果列表
     */
    @Override
    public List<UserExperienceResult> getMemberExperiences(Long memberId, String type) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFound("成员不存在"));

        if (!member.isTeamMember()) {
            log.info("成员 {} 不是团队成员，返回空经历列表", memberId);
            return Collections.emptyList();
        }

        List<com.bluenet.web.domain.model.entity.UserExperience> experiences;
        if (type != null && !type.isBlank()) {
            ExperienceType experienceType = parseExperienceType(type);
            experiences = userExperienceRepository.findByUserIdAndType(memberId, experienceType);
        } else {
            experiences = userExperienceRepository.findByUserId(memberId);
        }
        return experiences.stream()
                .map(this::toUserExperienceResult)
                .toList();
    }

    private ExperienceType parseExperienceType(String type) {
        try {
            return ExperienceType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequest("无效的经历类型: " + type);
        }
    }

    private UserExperienceResult toUserExperienceResult(com.bluenet.web.domain.model.entity.UserExperience experience) {
        return new UserExperienceResult(
                experience.getId(),
                experience.getType(),
                experience.getTitle(),
                formatDateTime(experience.getStartTime()),
                formatDateTime(experience.getEndTime()),
                experience.getContent());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
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
}
