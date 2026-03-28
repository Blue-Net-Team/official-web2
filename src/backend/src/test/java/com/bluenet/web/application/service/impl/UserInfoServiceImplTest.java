package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.converter.UserConverter;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.UserDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserInfoServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserInfoServiceImplTest {

    @Mock
    private UserConverter userConverter;

    @Mock
    private UserDomainService userDomainService;

    @InjectMocks
    private UserInfoServiceImpl userInfoService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_USERNAME = "测试用户";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ROLE_NAME = "MEMBER";

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
}
