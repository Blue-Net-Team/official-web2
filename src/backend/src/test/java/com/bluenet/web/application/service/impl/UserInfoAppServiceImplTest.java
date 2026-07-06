package com.bluenet.web.application.service.impl;

import com.bluenet.web.domain.model.enumerate.RoleType;

import com.bluenet.web.application.command.userinfo.UserInfoCommands;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.template.EmailVerificationCodeTemplate;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.UserDomainService;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.change.ChangePasswordStateService;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserInfoAppServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserInfoAppServiceImplTest {

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeDomainService verificationCodeDomainService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ChangePasswordStateService changePasswordStateService;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private FileDomainService fileDomainService;

    @Mock
    private MessageDispatcher messageDispatcher;

    @Mock
    private EmailVerificationCodeTemplate emailVerificationCodeTemplate;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private RoleTypeResolver roleTypeResolver;

    @InjectMocks
    private UserInfoAppServiceImpl userInfoAppService;

    @BeforeEach
    void setUp() {
        lenient().when(roleTypeResolver.resolve(anyLong())).thenAnswer(invocation -> {
            Long roleId = invocation.getArgument(0);
            return Arrays.stream(RoleType.values())
                    .filter(rt -> (long) rt.getLevel() == roleId)
                    .findFirst()
                    .orElse(null);
        });
    }

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_USERNAME = "测试用户";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ROLE_NAME = "MEMBER";
    private static final String TEST_PASSWORD = "encoded_old_pwd";

    private User createMemberUser() {
        User user = User.reconstruct(TEST_USER_ID, "password");
        user.setStudentId(TEST_STUDENT_ID);
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setRoleId((long) RoleType.fromName(TEST_ROLE_NAME).getLevel());
        user.setMajor("软件工程");
        user.setDirection(Direction.COMPUTER_VISION);
        user.setGender(Gender.UNKNOWN);
        return user;
    }

    private User createCandidateUser() {
        User user = User.reconstruct(TEST_USER_ID, "password");
        user.setStudentId(TEST_STUDENT_ID);
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setRoleId((long) RoleType.fromName("CANDIDATE").getLevel());
        return user;
    }

    @Test
    void getMyInfo_whenUserExists_returnsConvertedUserInfo() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createMemberUser()));

        var result = userInfoAppService.getMyInfo(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_USERNAME, result.username());
        assertEquals(TEST_EMAIL, result.email());
        assertEquals(TEST_ROLE_NAME, result.roleName());
    }

    @Test
    void getMyInfo_whenUserNotFound_throwsUnauthorized() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        Unauthorized ex = assertThrows(Unauthorized.class, () -> userInfoAppService.getMyInfo(TEST_USER_ID));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void updateProfile_whenCandidateModifiesUsername_throwsForbidden() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createCandidateUser()));

        UserInfoCommands.UpdateProfileCommand command = new UserInfoCommands.UpdateProfileCommand(
                "newUsername", null, null, null, null, null, null, null);

        Forbidden ex = assertThrows(Forbidden.class, () -> userInfoAppService.updateProfile(TEST_USER_ID, command));
        assertEquals("只有成员及以上角色才能修改用户名、性别、学院、专业和报名方向", ex.getMessage());
    }

    @Test
    void updateProfile_whenMemberModifiesUsername_succeeds() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createMemberUser()));

        UserInfoCommands.UpdateProfileCommand command = new UserInfoCommands.UpdateProfileCommand(
                "newUsername", "newNickname", null, null, null, null, null, null);

        userInfoAppService.updateProfile(TEST_USER_ID, command);

        verify(userDomainService).updateProfile(
                TEST_USER_ID,
                "newUsername",
                "newNickname",
                null,
                null,
                null,
                null,
                null,
                null);
    }

    // --- changePassword tests ---

    @Test
    void verifyCurrentPassword_whenPasswordCorrect_returnsToken() {
        User user = User.reconstruct(TEST_USER_ID, "password");
        user.setPassword(TEST_PASSWORD);
        ;
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPwd", TEST_PASSWORD)).thenReturn(true);
        when(changePasswordStateService.create(TEST_USER_ID)).thenReturn("test-token");

        String token = userInfoAppService.verifyCurrentPassword(
                new UserInfoCommands.VerifyCurrentPasswordCommand(TEST_USER_ID, "correctPwd"));

        assertEquals("test-token", token);
        verify(changePasswordStateService).create(TEST_USER_ID);
    }

    @Test
    void verifyCurrentPassword_whenPasswordWrong_throwsBadRequest() {
        User user = User.reconstruct(TEST_USER_ID, "password");
        user.setPassword(TEST_PASSWORD);
        ;
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPwd", TEST_PASSWORD)).thenReturn(false);

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userInfoAppService.verifyCurrentPassword(
                        new UserInfoCommands.VerifyCurrentPasswordCommand(TEST_USER_ID, "wrongPwd")));

        assertEquals("当前密码不正确", ex.getMessage());
        verify(changePasswordStateService, never()).create(anyLong());
    }

    @Test
    void verifyCurrentPassword_whenUserNotFound_throwsUnauthorized() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                Unauthorized.class,
                () -> userInfoAppService.verifyCurrentPassword(
                        new UserInfoCommands.VerifyCurrentPasswordCommand(TEST_USER_ID, "somePwd")));
    }

    @Test
    void changePassword_whenValid_succeeds() {
        when(changePasswordStateService.exists("valid-token")).thenReturn(true);
        when(changePasswordStateService.getStep("valid-token")).thenReturn(1);
        when(changePasswordStateService.getField("valid-token", "userId")).thenReturn("1");

        userInfoAppService.changePassword(
                new UserInfoCommands.ChangePasswordCommand(TEST_USER_ID, "valid-token", "newPwd123", "newPwd123"));

        verify(userDomainService).changePassword(TEST_USER_ID, "newPwd123");
        verify(authTokenService).revokeAllUserTokens(TEST_USER_ID);
        verify(changePasswordStateService).delete("valid-token");
    }

    @Test
    void changePassword_whenTokenExpired_throwsBadRequest() {
        when(changePasswordStateService.exists("expired-token")).thenReturn(false);

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userInfoAppService.changePassword(
                        new UserInfoCommands.ChangePasswordCommand(TEST_USER_ID, "expired-token", "newPwd", "newPwd")));

        assertEquals("验证已过期，请重新开始", ex.getMessage());
        verify(userDomainService, never()).changePassword(anyLong(), anyString());
    }

    @Test
    void changePassword_whenStepNotVerified_throwsBadRequest() {
        when(changePasswordStateService.exists("token")).thenReturn(true);
        when(changePasswordStateService.getStep("token")).thenReturn(0);

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userInfoAppService.changePassword(
                        new UserInfoCommands.ChangePasswordCommand(TEST_USER_ID, "token", "newPwd", "newPwd")));

        assertEquals("请先验证当前密码", ex.getMessage());
    }

    @Test
    void changePassword_whenPasswordsMismatch_throwsBadRequest() {
        when(changePasswordStateService.exists("token")).thenReturn(true);
        when(changePasswordStateService.getStep("token")).thenReturn(1);
        when(changePasswordStateService.getField("token", "userId")).thenReturn("1");

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userInfoAppService.changePassword(
                        new UserInfoCommands.ChangePasswordCommand(TEST_USER_ID, "token", "newPwd1", "newPwd2")));

        assertEquals("两次输入的密码不一致", ex.getMessage());
        verify(userDomainService, never()).changePassword(anyLong(), anyString());
    }

    @Test
    void changePassword_whenUserIdMismatch_throwsBadRequest() {
        when(changePasswordStateService.exists("token")).thenReturn(true);
        when(changePasswordStateService.getStep("token")).thenReturn(1);
        when(changePasswordStateService.getField("token", "userId")).thenReturn("999");

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userInfoAppService.changePassword(
                        new UserInfoCommands.ChangePasswordCommand(TEST_USER_ID, "token", "newPwd", "newPwd")));

        assertEquals("验证信息不匹配", ex.getMessage());
    }

    // ==================== wechatQrcode 测试 ====================

    @Test
    void getMyInfo_whenUserHasQrcode_returnsWechatQrcode() {
        User userVO = User.reconstruct(TEST_USER_ID, "password");
        userVO.setStudentId(TEST_STUDENT_ID);
        userVO.setUsername(TEST_USERNAME);
        userVO.setEmail(TEST_EMAIL);
        userVO.setRoleId((long) RoleType.fromName(TEST_ROLE_NAME).getLevel());
        userVO.setMajor("软件工程");
        userVO.setQrcodeId(123L);
        ;

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));

        var result = userInfoAppService.getMyInfo(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(123L, result.wechatQrcode());
    }

    @Test
    void updateProfile_withQrcodeFileId_succeeds() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createMemberUser()));

        UserInfoCommands.UpdateProfileCommand command = new UserInfoCommands.UpdateProfileCommand(
                null, "newNickname", null, null, null, null, null, 100L);

        userInfoAppService.updateProfile(TEST_USER_ID, command);

        verify(userDomainService).updateProfile(
                TEST_USER_ID,
                null,
                "newNickname",
                null,
                null,
                null,
                null,
                null,
                100L);
    }

    // ==================== updateAvatar 测试 ====================

    @Nested
    @DisplayName("updateAvatar 方法测试")
    class UpdateAvatarTests {

        @Test
        @DisplayName("TC-201: 成功更新头像")
        void updateAvatar_success() {
            FileVO fileVO = FileVO.builder().id(100L).type(FileType.AVATAR).build();
            when(fileDomainService.getFileById(100L)).thenReturn(fileVO);

            userInfoAppService.updateAvatar(TEST_USER_ID, new UserInfoCommands.UpdateAvatarCommand(100L));

            verify(fileDomainService).getFileById(100L);
            verify(userDomainService).updateUserAvatar(TEST_USER_ID, fileVO);
        }

        @Test
        @DisplayName("TC-202: 文件不存在应抛出DataNotFound")
        void updateAvatar_fileNotFound_throwsDataNotFound() {
            when(fileDomainService.getFileById(9999L)).thenReturn(null);

            DataNotFound ex = assertThrows(
                    DataNotFound.class,
                    () -> userInfoAppService
                            .updateAvatar(TEST_USER_ID, new UserInfoCommands.UpdateAvatarCommand(9999L)));
            assertEquals("文件不存在", ex.getMessage());
            verify(userDomainService, never()).updateUserAvatar(anyLong(), any());
        }

        @Test
        @DisplayName("TC-203: 文件类型不匹配应抛出BadRequest")
        void updateAvatar_fileTypeMismatch_throwsBadRequest() {
            FileVO fileVO = FileVO.builder().id(100L).type(FileType.WORK).build();
            when(fileDomainService.getFileById(100L)).thenReturn(fileVO);

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> userInfoAppService
                            .updateAvatar(TEST_USER_ID, new UserInfoCommands.UpdateAvatarCommand(100L)));
            assertEquals("文件类型不匹配，期望 AVATAR", ex.getMessage());
            verify(userDomainService, never()).updateUserAvatar(anyLong(), any());
        }
    }
}
