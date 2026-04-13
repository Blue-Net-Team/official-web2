package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.converter.UserConverter;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.UserDomainService;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.change.ChangePasswordStateService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.email.EmailSender;

/**
 * UserInfoServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserInfoServiceImplTest {

    @Mock
    private UserConverter userConverter;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private VerificationCodeDomainService verificationCodeDomainService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ChangePasswordStateService changePasswordStateService;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private FileDomainService fileDomainService;

    @InjectMocks
    private UserInfoServiceImpl userInfoService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_USERNAME = "测试用户";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ROLE_NAME = "MEMBER";
    private static final String TEST_PASSWORD = "encoded_old_pwd";

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    void getMyInfo_whenUserInContext_returnsConvertedUserInfo() {
        UserVO userVO = UserVO.builder()
                .id(TEST_USER_ID)
                .studentId(TEST_STUDENT_ID)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .roleName(TEST_ROLE_NAME)
                .college("计算机学院")
                .major("软件工程")
                .direction(Direction.COMPUTER_VISION)
                .gender(Gender.UNKNOWN)
                .build();

        UserInfo expectedInfo = UserInfo.builder()
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .college("计算机学院")
                .major("软件工程")
                .roleName(TEST_ROLE_NAME)
                .direction(Direction.COMPUTER_VISION)
                .gender(Gender.UNKNOWN)
                .build();

        UserCTX.setCurrentUser(userVO);
        when(userConverter.convertToUserInfo(userVO)).thenReturn(expectedInfo);

        UserInfo result = userInfoService.getMyInfo();

        assertNotNull(result);
        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_ROLE_NAME, result.getRoleName());
        verify(userConverter).convertToUserInfo(userVO);
    }

    @Test
    void getMyInfo_whenNoUserInContext_throwsUnauthorized() {
        UserCTX.clear();
        assertNull(UserCTX.getCurrentUser());

        Unauthorized ex = assertThrows(Unauthorized.class, () -> userInfoService.getMyInfo());

        assertEquals("未认证", ex.getMessage());
        verify(userConverter, org.mockito.Mockito.never()).convertToUserInfo(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateProfile_whenCandidateModifiesUsername_throwsForbidden() {
        UserVO candidateUser = UserVO.builder()
                .id(TEST_USER_ID)
                .studentId(TEST_STUDENT_ID)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .roleName("CANDIDATE")
                .build();
        UserCTX.setCurrentUser(candidateUser);

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setUsername("newUsername");

        Forbidden ex = assertThrows(Forbidden.class, () -> userInfoService.updateProfile(request));

        assertEquals("只有成员及以上角色才能修改用户名、性别、学院、专业和报名方向", ex.getMessage());
    }

    @Test
    void updateProfile_whenMemberModifiesUsername_succeeds() {
        UserVO memberUser = UserVO.builder()
                .id(TEST_USER_ID)
                .studentId(TEST_STUDENT_ID)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .roleName("MEMBER")
                .build();
        UserCTX.setCurrentUser(memberUser);

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setUsername("newUsername");
        request.setNickname("newNickname");

        userInfoService.updateProfile(request);

        verify(userDomainService).updateProfile(
                TEST_USER_ID,
                "newUsername",
                "newNickname",
                null,
                null,
                null,
                null,
                null);
    }

    // --- changePassword tests ---

    @Test
    void verifyCurrentPassword_whenPasswordCorrect_returnsToken() {
        UserVO user = UserVO.builder()
                .id(TEST_USER_ID)
                .password(TEST_PASSWORD)
                .build();
        UserCTX.setCurrentUser(user);
        when(passwordEncoder.matches("correctPwd", TEST_PASSWORD)).thenReturn(true);
        when(changePasswordStateService.create(TEST_USER_ID)).thenReturn("test-token");

        String token = userInfoService.verifyCurrentPassword(TEST_USER_ID, "correctPwd");

        assertEquals("test-token", token);
        verify(changePasswordStateService).create(TEST_USER_ID);
    }

    @Test
    void verifyCurrentPassword_whenPasswordWrong_throwsBadRequest() {
        UserVO user = UserVO.builder()
                .id(TEST_USER_ID)
                .password(TEST_PASSWORD)
                .build();
        UserCTX.setCurrentUser(user);
        when(passwordEncoder.matches("wrongPwd", TEST_PASSWORD)).thenReturn(false);

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userInfoService.verifyCurrentPassword(TEST_USER_ID, "wrongPwd"));

        assertEquals("当前密码不正确", ex.getMessage());
        verify(changePasswordStateService, never()).create(anyLong());
    }

    @Test
    void verifyCurrentPassword_whenNotAuthenticated_throwsUnauthorized() {
        UserCTX.clear();

        assertThrows(
                Unauthorized.class,
                () -> userInfoService.verifyCurrentPassword(TEST_USER_ID, "somePwd"));
    }

    @Test
    void changePassword_whenValid_succeeds() {
        when(changePasswordStateService.exists("valid-token")).thenReturn(true);
        when(changePasswordStateService.getStep("valid-token")).thenReturn(1);
        when(changePasswordStateService.getField("valid-token", "userId")).thenReturn("1");

        userInfoService.changePassword(TEST_USER_ID, "valid-token", "newPwd123", "newPwd123");

        verify(userDomainService).changePassword(TEST_USER_ID, "newPwd123");
        verify(authTokenService).revokeAllUserTokens(TEST_USER_ID);
        verify(changePasswordStateService).delete("valid-token");
    }

    @Test
    void changePassword_whenTokenExpired_throwsBadRequest() {
        when(changePasswordStateService.exists("expired-token")).thenReturn(false);

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userInfoService.changePassword(TEST_USER_ID, "expired-token", "newPwd", "newPwd"));

        assertEquals("验证已过期，请重新开始", ex.getMessage());
        verify(userDomainService, never()).changePassword(anyLong(), anyString());
    }

    @Test
    void changePassword_whenStepNotVerified_throwsBadRequest() {
        when(changePasswordStateService.exists("token")).thenReturn(true);
        when(changePasswordStateService.getStep("token")).thenReturn(0);

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userInfoService.changePassword(TEST_USER_ID, "token", "newPwd", "newPwd"));

        assertEquals("请先验证当前密码", ex.getMessage());
    }

    @Test
    void changePassword_whenPasswordsMismatch_throwsBadRequest() {
        when(changePasswordStateService.exists("token")).thenReturn(true);
        when(changePasswordStateService.getStep("token")).thenReturn(1);
        when(changePasswordStateService.getField("token", "userId")).thenReturn("1");

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userInfoService.changePassword(TEST_USER_ID, "token", "newPwd1", "newPwd2"));

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
                () -> userInfoService.changePassword(TEST_USER_ID, "token", "newPwd", "newPwd"));

        assertEquals("验证信息不匹配", ex.getMessage());
    }

    // ==================== updateAvatar 测试 ====================

    @Nested
    @DisplayName("updateAvatar 方法测试")
    class UpdateAvatarTests {

        @Test
        @DisplayName("TC-201: 成功更新头像")
        void updateAvatar_success() {
            UserVO user = UserVO.builder().id(TEST_USER_ID).build();
            UserCTX.setCurrentUser(user);

            FileVO fileVO = FileVO.builder().id(100L).type(FileType.AVATAR).build();
            when(fileDomainService.getFileById(100L)).thenReturn(fileVO);

            userInfoService.updateAvatar(100L);

            verify(fileDomainService).getFileById(100L);
            verify(userDomainService).updateUserAvatar(TEST_USER_ID, fileVO);
        }

        @Test
        @DisplayName("TC-202: 文件不存在应抛出DataNotFound")
        void updateAvatar_fileNotFound_throwsDataNotFound() {
            UserVO user = UserVO.builder().id(TEST_USER_ID).build();
            UserCTX.setCurrentUser(user);

            when(fileDomainService.getFileById(9999L)).thenReturn(null);

            DataNotFound ex = assertThrows(DataNotFound.class, () -> userInfoService.updateAvatar(9999L));
            assertEquals("文件不存在", ex.getMessage());
            verify(userDomainService, never()).updateUserAvatar(anyLong(), any());
        }

        @Test
        @DisplayName("TC-203: 文件类型不匹配应抛出BadRequest")
        void updateAvatar_fileTypeMismatch_throwsBadRequest() {
            UserVO user = UserVO.builder().id(TEST_USER_ID).build();
            UserCTX.setCurrentUser(user);

            FileVO fileVO = FileVO.builder().id(100L).type(FileType.WORK).build();
            when(fileDomainService.getFileById(100L)).thenReturn(fileVO);

            BadRequest ex = assertThrows(BadRequest.class, () -> userInfoService.updateAvatar(100L));
            assertEquals("文件类型不匹配，期望 AVATAR", ex.getMessage());
            verify(userDomainService, never()).updateUserAvatar(anyLong(), any());
        }

        @Test
        @DisplayName("TC-204: 未登录应抛出Unauthorized")
        void updateAvatar_notAuthenticated_throwsUnauthorized() {
            UserCTX.clear();

            assertThrows(Unauthorized.class, () -> userInfoService.updateAvatar(100L));
            verify(fileDomainService, never()).getFileById(anyLong());
            verify(userDomainService, never()).updateUserAvatar(anyLong(), any());
        }
    }
}
