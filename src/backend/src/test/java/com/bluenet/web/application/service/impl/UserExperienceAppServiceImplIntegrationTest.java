package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.userexperience.UserExperienceCommands;
import com.bluenet.web.application.result.user.UserExperienceResult;
import com.bluenet.web.application.service.UserExperienceAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.UserFixture;
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
 *
 * <p>
 * 按新测试策略：真实 Repository，使用测试夹具创建用户。本类验证应用服务层的编排、 事务边界与响应格式。
 * </p>
 */
@DisplayName("UserExperienceAppServiceImpl 集成测试")
class UserExperienceAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserExperienceAppService userExperienceAppService;

    @Autowired
    private UserExperienceRepository userExperienceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User currentUser;

    @BeforeEach
    void prepare() {
        currentUser = UserFixture.member("2026001001").save(userRepository, passwordEncoder);
        UserCTX.setPrincipal(
                new SecurityPrincipal(currentUser, com.bluenet.web.domain.model.enumerate.RoleType.MEMBER,
                        java.util.Collections.emptySet()));
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
                null);

        assertThrows(
                BadRequest.class,
                () -> userExperienceAppService.createExperience(command));
    }

    @Test
    @DisplayName("createExperience: 竞赛类型已下线应抛异常")
    void createExperience_competition_shouldThrow() {
        UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                "COMPETITION",
                "蓝桥杯",
                "参赛者",
                "2024.03",
                null,
                "描述",
                null,
                null,
                null,
                null,
                null,
                null);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> userExperienceAppService.createExperience(command));
        assertEquals("竞赛经历已下线，请联系管理员在成就系统中维护", exception.getMessage());
    }

    @Test
    @DisplayName("getExperiences: 应按类型过滤")
    void getExperiences_shouldFilterByType() {
        createProject("项目A");
        createInternship("公司B");

        List<UserExperienceResult> projects = userExperienceAppService.getExperiences("PROJECT");
        List<UserExperienceResult> internships = userExperienceAppService.getExperiences("INTERNSHIP");

        assertEquals(1, projects.size());
        assertEquals("项目A", projects.get(0).title());
        assertEquals(1, internships.size());
        assertEquals("公司B", internships.get(0).title());
    }

    @Test
    @DisplayName("getExperiences: 查询竞赛类型应返回空列表")
    void getExperiences_competition_shouldReturnEmpty() {
        createProject("项目A");

        List<UserExperienceResult> competitions = userExperienceAppService.getExperiences("COMPETITION");

        assertTrue(competitions.isEmpty());
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
        UserCTX.setPrincipal(
                new SecurityPrincipal(otherUser, com.bluenet.web.domain.model.enumerate.RoleType.MEMBER,
                        java.util.Collections.emptySet()));

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
        UserCTX.setPrincipal(
                new SecurityPrincipal(otherUser, com.bluenet.web.domain.model.enumerate.RoleType.MEMBER,
                        java.util.Collections.emptySet()));

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
                null);
        return userExperienceAppService.createExperience(command);
    }

    private UserExperienceResult createInternship(String company) {
        UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                "INTERNSHIP",
                null,
                null,
                "2024.03",
                null,
                "描述",
                null,
                null,
                company,
                "实习生",
                "active",
                null);
        return userExperienceAppService.createExperience(command);
    }

    private User createOtherUser(String studentId) {
        return UserFixture.member(studentId).save(userRepository, passwordEncoder);
    }
}
