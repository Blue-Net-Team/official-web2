package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.UserExperienceResult;
import com.bluenet.web.application.command.member.MemberCommands;
import com.bluenet.web.application.service.MemberAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MemberAppServiceImpl 集成测试。
 */
@DisplayName("MemberAppServiceImpl 集成测试")
class MemberAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MemberAppService memberAppService;

    @Autowired
    private UserExperienceRepository userExperienceRepository;

    @Autowired
    private com.bluenet.web.domain.repository.UserRepository userRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User createMember(String studentId) {
        RoleDO memberRole = roleMapper.selectByName(RoleType.MEMBER.getName());
        User user = User.create(
                studentId,
                studentId + "@example.com",
                memberRole.getId(),
                passwordEncoder.encode("pwd"),
                "用户" + studentId,
                "昵称" + studentId,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                Gender.MALE,
                "开发",
                null,
                null,
                null,
                null,
                "REF" + studentId.substring(studentId.length() - 5),
                "bio");
        userRepository.save(user);
        return user;
    }

    private User createCandidate(String studentId) {
        RoleDO candidateRole = roleMapper.selectByName(RoleType.CANDIDATE.getName());
        User user = User.create(
                studentId,
                studentId + "@example.com",
                candidateRole.getId(),
                passwordEncoder.encode("pwd"),
                "用户" + studentId,
                "昵称" + studentId,
                null,
                null,
                null,
                null,
                Gender.UNKNOWN,
                null,
                null,
                null,
                null,
                null,
                "REF" + studentId.substring(studentId.length() - 5),
                null);
        userRepository.save(user);
        return user;
    }

    private void addExperience(Long userId, ExperienceType type, String title) {
        com.bluenet.web.domain.model.entity.UserExperience experience = com.bluenet.web.domain.model.entity.UserExperience
                .create(userId, type, title, "{}", LocalDateTime.now(), null);
        userExperienceRepository.save(experience);
    }

    @Test
    @DisplayName("getMemberExperiences: 应返回成员经历列表")
    void getMemberExperiences_shouldReturnExperiences() {
        User member = createMember("2026002001");
        addExperience(member.getId(), ExperienceType.PROJECT, "项目A");
        addExperience(member.getId(), ExperienceType.COMPETITION, "竞赛B");

        List<UserExperienceResult> results = memberAppService.getMemberExperiences(member.getId(), null);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("getMemberExperiences: 应按类型过滤")
    void getMemberExperiences_shouldFilterByType() {
        User member = createMember("2026002002");
        addExperience(member.getId(), ExperienceType.PROJECT, "项目A");
        addExperience(member.getId(), ExperienceType.COMPETITION, "竞赛B");

        List<UserExperienceResult> results = memberAppService.getMemberExperiences(member.getId(), "PROJECT");

        assertEquals(1, results.size());
        assertEquals("项目A", results.get(0).title());
    }

    @Test
    @DisplayName("getMemberExperiences: 无效类型应抛异常")
    void getMemberExperiences_invalidType_shouldThrow() {
        User member = createMember("2026002003");
        assertThrows(
                com.bluenet.web.domain.exception.BadRequest.class,
                () -> memberAppService.getMemberExperiences(member.getId(), "INVALID"));
    }

    @Test
    @DisplayName("getMemberExperiences: 非团队成员应返回空列表")
    void getMemberExperiences_nonTeamMember_shouldReturnEmpty() {
        User candidate = createCandidate("2026002004");

        List<UserExperienceResult> results = memberAppService.getMemberExperiences(candidate.getId(), null);

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("getMemberExperiences: 成员不存在应抛异常")
    void getMemberExperiences_memberNotFound_shouldThrow() {
        assertThrows(
                DataNotFound.class,
                () -> memberAppService.getMemberExperiences(-1L, null));
    }

    @Test
    @DisplayName("getMemberById: 应返回成员详情")
    void getMemberById_shouldReturnMember() {
        User member = createMember("2026002005");

        var result = memberAppService.getMemberById(member.getId());

        assertEquals(member.getId(), result.id());
        assertEquals("用户2026002005", result.username());
    }

    @Test
    @DisplayName("getMemberList: 应分页返回成员列表")
    void getMemberList_shouldReturnPage() {
        createMember("2026002006");

        var page = memberAppService.getMemberList(new MemberCommands.GetMemberListCommand(null, 0, 20));

        assertFalse(page.isEmpty());
    }
}
