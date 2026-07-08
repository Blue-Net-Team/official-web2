package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.result.user.UserExperienceResult;
import com.bluenet.web.application.command.userexperience.UserExperienceCommands;
import com.bluenet.web.application.service.UserExperienceAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserExperienceAppServiceImpl 集成测试。
 */
@DisplayName("UserExperienceAppServiceImpl 集成测试")
class UserExperienceAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserExperienceAppService userExperienceAppService;

    @Autowired
    private UserExperienceRepository userExperienceRepository;

    @Autowired
    private com.bluenet.web.domain.repository.UserRepository userRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User currentUser;

    @BeforeEach
    void prepare() {
        RoleDO role = roleMapper.selectByName(RoleType.MEMBER.getName());
        currentUser = User.create(
                "2026001001",
                "2026001001@example.com",
                role.getId(),
                passwordEncoder.encode("pwd"),
                "用户2026001001",
                "昵称",
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
                "REF00001",
                null);
        userRepository.save(currentUser);
        UserCTX.setPrincipal(new SecurityPrincipal(currentUser, RoleType.MEMBER, java.util.Collections.emptySet()));
    }

    @AfterEach
    void cleanup() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("getExperiences: 未登录应抛异常")
    void getExperiences_notAuthenticated_shouldThrow() {
        UserCTX.clear();
        assertThrows(Unauthorized.class, () -> userExperienceAppService.getExperiences(null));
    }

    @Test
    @DisplayName("createExperience: 应创建项目经历")
    void createExperience_project_shouldCreate() {
        UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                "PROJECT",
                "项目名称",
                "开发者",
                "2024.01",
                "2024.12",
                "项目描述",
                List.of("Java", "Spring"),
                "https://demo.example.com",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        UserExperienceResult result = userExperienceAppService.createExperience(command);

        assertNotNull(result.id());
        assertEquals(ExperienceType.PROJECT, result.type());
        assertEquals("项目名称", result.title());
        assertEquals("2024.01", result.startTime());
        assertTrue(userExperienceRepository.findById(result.id()).isPresent());
    }

    @Test
    @DisplayName("createExperience: 无效类型应抛异常")
    void createExperience_invalidType_shouldThrow() {
        UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                "INVALID",
                "名称",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(
                IllegalArgumentException.class,
                () -> userExperienceAppService.createExperience(command));
    }

    @Test
    @DisplayName("getExperiences: 应按类型过滤")
    void getExperiences_shouldFilterByType() {
        createProject("项目A");
        createCompetition("竞赛B");

        List<UserExperienceResult> projects = userExperienceAppService.getExperiences("PROJECT");
        List<UserExperienceResult> competitions = userExperienceAppService.getExperiences("COMPETITION");

        assertEquals(1, projects.size());
        assertEquals("项目A", projects.get(0).title());
        assertEquals(1, competitions.size());
        assertEquals("竞赛B", competitions.get(0).title());
    }

    @Test
    @DisplayName("updateExperience: 应更新自己的经历")
    void updateExperience_owner_shouldUpdate() {
        UserExperienceResult created = createProject("旧项目");
        UserExperienceCommands.UpdateExperienceCommand command = new UserExperienceCommands.UpdateExperienceCommand(
                created.id(),
                "新项目",
                "架构师",
                "2025.01",
                "2025.06",
                "新描述",
                List.of("Go"),
                "https://new.example.com",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        UserExperienceResult updated = userExperienceAppService.updateExperience(command);

        assertEquals("新项目", updated.title());
        assertEquals("2025.01", updated.startTime());
        assertEquals("2025.06", updated.endTime());
    }

    @Test
    @DisplayName("updateExperience: 非自己经历应被禁止")
    void updateExperience_notOwner_shouldThrow() {
        UserExperienceResult created = createProject("项目");
        User otherUser = createOtherUser("2026001002");
        UserCTX.setPrincipal(new SecurityPrincipal(otherUser, RoleType.MEMBER, java.util.Collections.emptySet()));

        UserExperienceCommands.UpdateExperienceCommand command = new UserExperienceCommands.UpdateExperienceCommand(
                created.id(),
                "篡改",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(Forbidden.class, () -> userExperienceAppService.updateExperience(command));
    }

    @Test
    @DisplayName("deleteExperience: 应删除自己的经历")
    void deleteExperience_owner_shouldDelete() {
        UserExperienceResult created = createProject("待删除");

        userExperienceAppService.deleteExperience(created.id());

        assertTrue(userExperienceRepository.findById(created.id()).isEmpty());
    }

    @Test
    @DisplayName("deleteExperience: 非自己经历应被禁止")
    void deleteExperience_notOwner_shouldThrow() {
        UserExperienceResult created = createProject("项目");
        User otherUser = createOtherUser("2026001003");
        UserCTX.setPrincipal(new SecurityPrincipal(otherUser, RoleType.MEMBER, java.util.Collections.emptySet()));

        assertThrows(Forbidden.class, () -> userExperienceAppService.deleteExperience(created.id()));
    }

    @Test
    @DisplayName("deleteExperience: 经历不存在应抛异常")
    void deleteExperience_notFound_shouldThrow() {
        assertThrows(DataNotFound.class, () -> userExperienceAppService.deleteExperience(-1L));
    }

    private UserExperienceResult createProject(String name) {
        UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                "PROJECT",
                name,
                "开发者",
                "2024.01",
                null,
                "描述",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        return userExperienceAppService.createExperience(command);
    }

    private UserExperienceResult createCompetition(String name) {
        UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                "COMPETITION",
                name,
                "参赛者",
                "2024.03",
                null,
                "描述",
                null,
                null,
                "2024.04",
                "国家级",
                "一等奖",
                3,
                null,
                null,
                null,
                null,
                null);
        return userExperienceAppService.createExperience(command);
    }

    private User createOtherUser(String studentId) {
        RoleDO role = roleMapper.selectByName(RoleType.MEMBER.getName());
        User user = User.create(
                studentId,
                studentId + "@example.com",
                role.getId(),
                passwordEncoder.encode("pwd"),
                "用户" + studentId,
                "昵称",
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
}
