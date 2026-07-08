package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.result.adminuser.AdminUserResult;
import com.bluenet.web.application.command.adminuser.AdminUserCommands;
import com.bluenet.web.application.query.adminuser.GetUserListQuery;
import com.bluenet.web.application.service.AdminUserAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AdminUserAppServiceImpl 集成测试。
 * <p>
 * 验证管理员用户管理应用服务在实体驱动改造后的行为，重点覆盖批量操作使用 saveAll 的路径。
 * </p>
 */
@DisplayName("AdminUserAppServiceImpl 集成测试")
class AdminUserAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AdminUserAppService adminUserAppService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long memberRoleId;
    private Long candidateRoleId;
    private Long collegeId;

    @BeforeEach
    void prepare() {
        // Flyway V1 初始化角色：1=SUPER_ADMIN, 2=DIRECTION_ADMIN, 3=MEMBER, 4=CANDIDATE
        memberRoleId = roleMapper.selectByName(RoleType.MEMBER.getName()).getId();
        candidateRoleId = roleMapper.selectByName(RoleType.CANDIDATE.getName()).getId();

        College college = College.create("计算机学院");
        collegeRepository.save(college);
        collegeId = college.getId();
    }

    @AfterEach
    void cleanupContext() {
        UserCTX.clear();
    }

    private User createMemberUser(String studentId) {
        User user = User.create(
                studentId,
                studentId + "@example.com",
                memberRoleId,
                passwordEncoder.encode("password"),
                "用户" + studentId,
                "昵称" + studentId,
                collegeId,
                "计算机",
                2024,
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

    @Test
    @DisplayName("createUser: 应创建普通用户并加密密码")
    void createUser_shouldCreateUserWithEncodedPassword() {
        AdminUserCommands.CreateUserCommand command = new AdminUserCommands.CreateUserCommand(
                "2024002001",
                "2024002001@example.com",
                "新用户",
                "plainPassword",
                "新昵称",
                memberRoleId,
                collegeId,
                "计算机",
                Direction.COMPUTER_VISION,
                Gender.MALE,
                "开发",
                2024);

        AdminUserResult.Created result = adminUserAppService.createUser(command);

        assertNotNull(result.id());
        User created = userRepository.findById(result.id()).orElseThrow();
        assertEquals("2024002001", created.getStudentId());
        assertTrue(passwordEncoder.matches("plainPassword", created.getPassword()));
    }

    @Test
    @DisplayName("createUser: 不允许创建超级管理员")
    void createUser_withSuperAdminRole_shouldThrow() {
        Long superAdminRoleId = roleMapper.selectByName(RoleType.SUPER_ADMIN.getName()).getId();
        AdminUserCommands.CreateUserCommand command = new AdminUserCommands.CreateUserCommand(
                "2024002002",
                "2024002002@example.com",
                "超管",
                "pwd",
                "昵称",
                superAdminRoleId,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(BadRequest.class, () -> adminUserAppService.createUser(command));
    }

    @Test
    @DisplayName("updateUser: 应更新用户字段")
    void updateUser_shouldUpdateFields() {
        User user = createMemberUser("2024002003");

        AdminUserCommands.UpdateUserCommand command = new AdminUserCommands.UpdateUserCommand(
                user.getId(),
                candidateRoleId,
                Direction.STRUCTURAL_DESIGN,
                true,
                "算法工程师",
                "2024002003",
                "new@example.com",
                "改名用户",
                "改名昵称",
                collegeId,
                "新专业",
                Gender.FEMALE,
                2025);

        adminUserAppService.updateUser(command);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(candidateRoleId, updated.getRoleId());
        assertEquals(Direction.STRUCTURAL_DESIGN, updated.getDirection());
        assertTrue(updated.getDisable());
        assertEquals("算法工程师", updated.getJob());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("改名用户", updated.getUsername());
        assertEquals("新专业", updated.getMajor());
        assertEquals(Gender.FEMALE, updated.getGender());
        assertEquals(2025, updated.getAssessmentGradeYear());
    }

    @Test
    @DisplayName("resetPassword: 应重置用户密码")
    void resetPassword_shouldUpdatePassword() {
        User user = createMemberUser("2024002004");

        AdminUserCommands.ResetPasswordCommand command = new AdminUserCommands.ResetPasswordCommand(
                user.getId(), "newPassword", "newPassword");
        adminUserAppService.resetPassword(command);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newPassword", updated.getPassword()));
    }

    @Test
    @DisplayName("resetPassword: 两次密码不一致应抛异常")
    void resetPassword_withMismatchedPasswords_shouldThrow() {
        User user = createMemberUser("2024002005");
        AdminUserCommands.ResetPasswordCommand command = new AdminUserCommands.ResetPasswordCommand(
                user.getId(), "pwd1", "pwd2");

        assertThrows(IllegalArgumentException.class, () -> adminUserAppService.resetPassword(command));
    }

    @Test
    @DisplayName("batchDisable: 应批量禁用/启用用户")
    void batchDisable_shouldUpdateDisableState() {
        User user1 = createMemberUser("2024002006");
        User user2 = createMemberUser("2024002007");

        adminUserAppService.batchDisable(
                new AdminUserCommands.BatchOperateCommand(List.of(user1.getId(), user2.getId())),
                true);

        assertTrue(userRepository.findById(user1.getId()).orElseThrow().getDisable());
        assertTrue(userRepository.findById(user2.getId()).orElseThrow().getDisable());
    }

    @Test
    @DisplayName("batchDisable: 不能操作超级管理员")
    void batchDisable_withSuperAdmin_shouldThrow() {
        RoleDO superAdminRole = roleMapper.selectByName(RoleType.SUPER_ADMIN.getName());
        User superAdmin = User.create(
                "2024002008",
                "2024002008@example.com",
                superAdminRole.getId(),
                passwordEncoder.encode("pwd"),
                "超管",
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
                "REF00008",
                null);
        userRepository.save(superAdmin);

        assertThrows(
                BadRequest.class,
                () -> adminUserAppService.batchDisable(
                        new AdminUserCommands.BatchOperateCommand(List.of(superAdmin.getId())),
                        true));
    }

    @Test
    @DisplayName("batchUpdateRole: 应批量更新用户角色")
    void batchUpdateRole_shouldUpdateRoles() {
        User user1 = createMemberUser("2024002009");
        User user2 = createMemberUser("2024002010");

        adminUserAppService.batchUpdateRole(
                new AdminUserCommands.BatchUpdateRoleCommand(List.of(user1.getId(), user2.getId()), candidateRoleId));

        assertEquals(candidateRoleId, userRepository.findById(user1.getId()).orElseThrow().getRoleId());
        assertEquals(candidateRoleId, userRepository.findById(user2.getId()).orElseThrow().getRoleId());
    }

    @Test
    @DisplayName("deleteUser: 应删除普通用户")
    void deleteUser_shouldRemoveUser() {
        User user = createMemberUser("2024002011");

        adminUserAppService.deleteUser(user.getId());

        assertTrue(userRepository.findById(user.getId()).isEmpty());
    }

    @Test
    @DisplayName("deleteUser: 不能删除系统账号")
    void deleteUser_systemUser_shouldThrow() {
        // 手动创建与系统账号相同 studentId 的用户，模拟系统账号保护逻辑
        Long superAdminRoleId = roleMapper.selectByName(RoleType.SUPER_ADMIN.getName()).getId();
        User systemUser = User.create(
                "000000000000",
                "system@example.com",
                superAdminRoleId,
                passwordEncoder.encode("pwd"),
                "system",
                null,
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
                "SYSTEM01",
                null);
        userRepository.save(systemUser);

        assertThrows(BadRequest.class, () -> adminUserAppService.deleteUser(systemUser.getId()));
    }

    @Test
    @DisplayName("deleteUser: 不能删除当前登录用户自己")
    void deleteUser_self_shouldThrow() {
        User user = createMemberUser("2024002012");
        UserCTX.setPrincipal(new SecurityPrincipal(user, RoleType.MEMBER, java.util.Collections.emptySet()));

        assertThrows(BadRequest.class, () -> adminUserAppService.deleteUser(user.getId()));
    }

    @Test
    @DisplayName("getUserList: 应按角色分页")
    void getUserList_shouldFilterByRole() {
        User member = createMemberUser("2024002013");
        User candidate = User.create(
                "2024002014",
                "2024002014@example.com",
                candidateRoleId,
                passwordEncoder.encode("pwd"),
                "考生",
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
                "REF00014",
                null);
        userRepository.save(candidate);

        Page<AdminUserResult.ListItem> page = adminUserAppService.getUserList(
                new GetUserListQuery(0, 20, memberRoleId, null, null, null));

        assertEquals(1, page.getTotalElements());
        assertEquals(member.getId(), page.getContent().get(0).id());
    }

    @Test
    @DisplayName("getUserDetail: 应返回用户详情")
    void getUserDetail_shouldReturnDetail() {
        User user = createMemberUser("2024002015");

        AdminUserResult.Detail detail = adminUserAppService.getUserDetail(user.getId());

        assertEquals(user.getId(), detail.id());
        assertEquals("用户2024002015", detail.username());
    }

    @Test
    @DisplayName("getUserDetail: 用户不存在应抛异常")
    void getUserDetail_notFound_shouldThrow() {
        assertThrows(DataNotFound.class, () -> adminUserAppService.getUserDetail(-1L));
    }
}
