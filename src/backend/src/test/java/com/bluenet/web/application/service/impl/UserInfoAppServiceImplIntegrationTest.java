package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.userinfo.UserInfoCommands;
import com.bluenet.web.application.result.common.TabCounts;
import com.bluenet.web.application.result.user.UserInfoResult;
import com.bluenet.web.application.service.UserInfoAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.change.ChangePasswordStateService;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserInfoAppServiceImpl 集成测试。
 *
 * <p>
 * 按新测试策略：真实 Repository，DomainService 中无 @Transactional 的用 @MockitoBean， 外部基础设施
 * mock。本类验证应用服务层的编排、事务边界与响应格式。
 * </p>
 */
@DisplayName("UserInfoAppServiceImpl 集成测试")
class UserInfoAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserInfoAppService userInfoAppService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ChangePasswordStateService changePasswordStateService;

    @MockitoBean
    private FileDomainService fileDomainService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @MockitoBean
    private VerificationCodeDomainService verificationCodeDomainService;

    @MockitoBean
    private MessageDispatcher messageDispatcher;

    private Long memberRoleId;
    private Long candidateRoleId;
    private Long collegeId;

    @BeforeEach
    void prepare() {
        memberRoleId = com.bluenet.web.testsupport.fixture.RoleFixture.defaultRoleId(RoleType.MEMBER);
        candidateRoleId = com.bluenet.web.testsupport.fixture.RoleFixture.defaultRoleId(RoleType.CANDIDATE);
        College college = CollegeFixture.saveDefaultCollege(collegeRepository);
        collegeId = college.getId();
    }

    private User createUser(String studentId, Long roleId) {
        return UserFixture.builder()
                .withStudentId(studentId)
                .withRoleId(roleId)
                .withCollegeId(collegeId)
                .withMajor("计算机")
                .withAssessmentGradeYear(2024)
                .withDirection(Direction.COMPUTER_VISION)
                .withGender(Gender.MALE)
                .withJob("开发")
                .withBio("bio")
                .save(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("getMyInfo: 应返回当前用户信息")
    void getMyInfo_shouldReturnUserInfo() {
        User user = createUser("2024003001", memberRoleId);

        UserInfoResult result = userInfoAppService.getMyInfo(user.getId());

        assertEquals(user.getId(), result.id());
        assertEquals("用户2024003001", result.username());
        assertEquals("大二", result.grade());
        assertEquals("计算机学院", result.college());
    }

    @Test
    @DisplayName("getMyInfo: 用户不存在应抛异常")
    void getMyInfo_userNotFound_shouldThrow() {
        assertThrows(Unauthorized.class, () -> userInfoAppService.getMyInfo(-1L));
    }

    @Test
    @DisplayName("updateProfile: 成员应能更新所有字段")
    void updateProfile_member_shouldUpdateAllFields() {
        User user = createUser("2024003002", memberRoleId);

        UserInfoCommands.UpdateProfileCommand command = new UserInfoCommands.UpdateProfileCommand(
                "新姓名",
                "新昵称",
                "计算机学院",
                "软件工程",
                Direction.STRUCTURAL_DESIGN,
                Gender.FEMALE,
                "新简介",
                null);

        userInfoAppService.updateProfile(user.getId(), command);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("新姓名", updated.getUsername());
        assertEquals("新昵称", updated.getNickname());
        assertEquals(collegeId, updated.getCollegeId());
        assertEquals("软件工程", updated.getMajor());
        assertEquals(Direction.STRUCTURAL_DESIGN, updated.getDirection());
        assertEquals(Gender.FEMALE, updated.getGender());
        assertEquals("新简介", updated.getBio());
    }

    @Test
    @DisplayName("updateProfile: 考生不能修改用户名、性别、学院、专业和方向")
    void updateProfile_candidate_shouldRestrictFields() {
        User user = createUser("2024003003", candidateRoleId);

        UserInfoCommands.UpdateProfileCommand command = new UserInfoCommands.UpdateProfileCommand(
                "新姓名",
                "新昵称",
                "计算机学院",
                "软件工程",
                Direction.STRUCTURAL_DESIGN,
                Gender.FEMALE,
                "新简介",
                null);

        assertThrows(Forbidden.class, () -> userInfoAppService.updateProfile(user.getId(), command));

        // 考生只允许修改昵称和简介
        UserInfoCommands.UpdateProfileCommand allowedCommand = new UserInfoCommands.UpdateProfileCommand(
                null,
                "新昵称",
                null,
                null,
                null,
                null,
                "新简介",
                null);

        assertDoesNotThrow(() -> userInfoAppService.updateProfile(user.getId(), allowedCommand));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("新昵称", updated.getNickname());
        assertEquals("新简介", updated.getBio());
        assertEquals("用户2024003003", updated.getUsername()); // 未变
    }

    @Test
    @DisplayName("updateProfile: 学院名不存在应抛异常")
    void updateProfile_collegeNotFound_shouldThrow() {
        User user = createUser("2024003004", memberRoleId);
        UserInfoCommands.UpdateProfileCommand command = new UserInfoCommands.UpdateProfileCommand(
                null,
                null,
                "不存在的学院",
                null,
                null,
                null,
                null,
                null);

        assertThrows(DataNotFound.class, () -> userInfoAppService.updateProfile(user.getId(), command));
    }

    @Test
    @DisplayName("updateProfile: 空字符串学院名应清空 college_id")
    void updateProfile_collegeBlank_shouldClearCollegeId() {
        User user = createUser("2024003013", memberRoleId);
        UserInfoCommands.UpdateProfileCommand command = new UserInfoCommands.UpdateProfileCommand(
                null,
                null,
                "",
                null,
                null,
                null,
                null,
                null);

        userInfoAppService.updateProfile(user.getId(), command);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertNull(updated.getCollegeId());
    }

    @Test
    @DisplayName("updateProfile: 成员修改学院名应更新 college_id")
    void updateProfile_member_shouldUpdateCollegeByName() {
        User user = createUser("2024003014", memberRoleId);
        College newCollege = CollegeFixture.saveCollege(collegeRepository, "软件学院");

        UserInfoCommands.UpdateProfileCommand command = new UserInfoCommands.UpdateProfileCommand(
                null,
                null,
                "软件学院",
                null,
                null,
                null,
                null,
                null);

        userInfoAppService.updateProfile(user.getId(), command);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(newCollege.getId(), updated.getCollegeId());
    }

    @Test
    @DisplayName("updateProfile: 不传学院时应保留原 college_id")
    void updateProfile_collegeNull_shouldKeepCollegeId() {
        User user = createUser("2024003012", memberRoleId);
        UserInfoCommands.UpdateProfileCommand command = new UserInfoCommands.UpdateProfileCommand(
                null,
                "新昵称",
                null,
                null,
                null,
                null,
                null,
                null);

        userInfoAppService.updateProfile(user.getId(), command);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("新昵称", updated.getNickname());
        assertEquals(collegeId, updated.getCollegeId());
    }

    @Test
    @DisplayName("updateAvatar: 应更新头像文件ID")
    void updateAvatar_shouldUpdateAvatarId() {
        User user = createUser("2024003005", memberRoleId);
        Long fileId = 3005L;
        File avatar = File.reconstruct(
                fileId,
                "avatar-3005.png",
                FileType.AVATAR,
                "http://example.com/avatar-3005.png",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        when(fileDomainService.getFileById(fileId)).thenReturn(avatar);

        userInfoAppService.updateAvatar(user.getId(), new UserInfoCommands.UpdateAvatarCommand(fileId));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(fileId, updated.getAvatarId());
    }

    @Test
    @DisplayName("updateAvatar: 文件类型不匹配应抛异常")
    void updateAvatar_wrongFileType_shouldThrow() {
        User user = createUser("2024003006", memberRoleId);
        Long fileId = 3006L;
        File normalImg = File.reconstruct(
                fileId,
                "normal-3006.png",
                FileType.NORMAL_IMG,
                "http://example.com/normal-3006.png",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        when(fileDomainService.getFileById(fileId)).thenReturn(normalImg);

        assertThrows(
                BadRequest.class,
                () -> userInfoAppService.updateAvatar(
                        user.getId(),
                        new UserInfoCommands.UpdateAvatarCommand(fileId)));
    }

    @Test
    @DisplayName("updateAvatar: 文件不存在应抛异常")
    void updateAvatar_fileNotFound_shouldThrow() {
        User user = createUser("2024003007", memberRoleId);
        Long fileId = -1L;
        when(fileDomainService.getFileById(fileId)).thenReturn(null);

        assertThrows(
                DataNotFound.class,
                () -> userInfoAppService.updateAvatar(user.getId(), new UserInfoCommands.UpdateAvatarCommand(fileId)));
    }

    @Test
    @DisplayName("changePassword: 应验证当前密码并修改新密码")
    void changePassword_shouldVerifyAndUpdate() {
        User user = createUser("2024003008", memberRoleId);

        String token = userInfoAppService.verifyCurrentPassword(
                new UserInfoCommands.VerifyCurrentPasswordCommand(user.getId(), "password"));

        assertNotNull(token);
        assertTrue(changePasswordStateService.exists(token));

        userInfoAppService.changePassword(
                new UserInfoCommands.ChangePasswordCommand(
                        user.getId(), token, "newPassword", "newPassword"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newPassword", updated.getPassword()));
        assertFalse(changePasswordStateService.exists(token));
        verify(authTokenService).revokeAllUserTokens(user.getId());
    }

    @Test
    @DisplayName("changePassword: 原密码错误应抛异常")
    void changePassword_wrongCurrentPassword_shouldThrow() {
        User user = createUser("2024003009", memberRoleId);

        assertThrows(
                BadRequest.class,
                () -> userInfoAppService.verifyCurrentPassword(
                        new UserInfoCommands.VerifyCurrentPasswordCommand(user.getId(), "wrongPassword")));
    }

    @Test
    @DisplayName("changePassword: 两次新密码不一致应抛异常")
    void changePassword_mismatchedNewPassword_shouldThrow() {
        User user = createUser("2024003010", memberRoleId);
        String token = userInfoAppService.verifyCurrentPassword(
                new UserInfoCommands.VerifyCurrentPasswordCommand(user.getId(), "password"));

        assertThrows(
                BadRequest.class,
                () -> userInfoAppService.changePassword(
                        new UserInfoCommands.ChangePasswordCommand(
                                user.getId(), token, "newPassword", "differentPassword")));
    }

    @Test
    @DisplayName("getTabCounts: 应返回用户关联统计")
    void getTabCounts_shouldReturnCounts() {
        User user = createUser("2024003011", memberRoleId);

        TabCounts tabCounts = userInfoAppService.getTabCounts(user.getId());

        assertEquals(0, tabCounts.getProjects());
        assertEquals(0, tabCounts.getCompetitions());
        assertEquals(0, tabCounts.getInternships());
    }
}
