package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.user.ChangeEmailRequestDTO;
import com.bluenet.web.api.dto.user.ChangePasswordRequestDTO;
import com.bluenet.web.api.dto.user.SendEmailVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UpdateAvatarRequestDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.api.dto.user.VerifyPasswordRequestDTO;
import com.bluenet.web.api.converter.userinfo.UserInfoResponseConverter;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.application.result.common.TabCounts;
import com.bluenet.web.application.result.user.UserInfoResult;
import com.bluenet.web.application.service.UserInfoAppService;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("UserProfileController 集成测试")
class UserProfileControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserInfoAppService userInfoAppService;

    @MockitoBean
    private UserInfoResponseConverter userInfoResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void getMyInfo_authenticated_returnsUserInfo() throws Exception {
        UserInfoResult result = new UserInfoResult(
                1L,
                "测试用户",
                "测试昵称",
                "计算机学院",
                "计算机科学与技术",
                "2024",
                "test@example.com",
                100L,
                "MEMBER",
                null,
                null,
                null,
                null,
                null,
                null);
        UserInfo userInfo = UserInfo.builder()
                .id(1L)
                .username("测试用户")
                .email("test@example.com")
                .build();
        when(userInfoAppService.getMyInfo(1L)).thenReturn(result);
        when(userInfoResponseConverter.toDTO(any(UserInfoResult.class))).thenReturn(userInfo);

        mockMvc.perform(get("/api/v1/user/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("测试用户"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void getMyInfo_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void updateProfile_authenticated_returnsOk() throws Exception {
        doNothing().when(userInfoAppService).updateProfile(any(Long.class), any());

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setNickname("新昵称");
        request.setBio("新简介");

        mockMvc.perform(
                put("/api/v1/user/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"));
    }

    @Test
    void updateProfile_anonymous_returnsUnauthorized() throws Exception {
        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setNickname("新昵称");

        mockMvc.perform(
                put("/api/v1/user/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void getTabCounts_authenticated_returnsCounts() throws Exception {
        TabCounts tabCounts = new TabCounts(1, 1, 0);
        TabCountsDTO dto = TabCountsDTO.builder()
                .projects(1)
                .competitions(1)
                .internships(0)
                .build();
        when(userInfoAppService.getTabCounts(1L)).thenReturn(tabCounts);
        when(userInfoResponseConverter.toTabCountsDTO(any(TabCounts.class))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/user/tab-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.projects").value(1))
                .andExpect(jsonPath("$.data.competitions").value(1))
                .andExpect(jsonPath("$.data.internships").value(0));
    }

    @Test
    void getTabCounts_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/tab-counts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void sendEmailVerificationCode_authenticated_returnsOk() throws Exception {
        doNothing().when(userInfoAppService).sendEmailVerificationCode(any());

        SendEmailVerificationCodeRequestDTO request = new SendEmailVerificationCodeRequestDTO();
        request.setEmail("test@example.com");
        request.setScene("change-email-original");

        mockMvc.perform(
                post("/api/v1/user/email/verification-code/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void sendEmailVerificationCode_anonymous_returnsUnauthorized() throws Exception {
        SendEmailVerificationCodeRequestDTO request = new SendEmailVerificationCodeRequestDTO();
        request.setEmail("test@example.com");
        request.setScene("change-email-original");

        mockMvc.perform(
                post("/api/v1/user/email/verification-code/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void changeEmail_withValidCodes_returnsOk() throws Exception {
        doNothing().when(userInfoAppService).changeEmail(any(Long.class), any());

        ChangeEmailRequestDTO request = new ChangeEmailRequestDTO();
        request.setOriginalEmailVerifyCode("123456");
        request.setNewEmail("new@example.com");
        request.setNewEmailVerifyCode("654321");

        mockMvc.perform(
                put("/api/v1/user/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void changeEmail_anonymous_returnsUnauthorized() throws Exception {
        ChangeEmailRequestDTO request = new ChangeEmailRequestDTO();
        request.setOriginalEmailVerifyCode("123456");
        request.setNewEmail("new@example.com");
        request.setNewEmailVerifyCode("654321");

        mockMvc.perform(
                put("/api/v1/user/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void verifyCurrentPassword_correct_returnsToken() throws Exception {
        when(userInfoAppService.verifyCurrentPassword(any())).thenReturn("verify-token");

        VerifyPasswordRequestDTO request = new VerifyPasswordRequestDTO();
        request.setCurrentPassword("password");

        mockMvc.perform(
                post("/api/v1/user/password/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("verify-token"));
    }

    @Test
    void verifyCurrentPassword_anonymous_returnsUnauthorized() throws Exception {
        VerifyPasswordRequestDTO request = new VerifyPasswordRequestDTO();
        request.setCurrentPassword("password");

        mockMvc.perform(
                post("/api/v1/user/password/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void changePassword_withValidToken_returnsOk() throws Exception {
        doNothing().when(userInfoAppService).changePassword(any());

        ChangePasswordRequestDTO changeRequest = new ChangePasswordRequestDTO();
        changeRequest.setToken("token");
        changeRequest.setNewPassword("newPassword");
        changeRequest.setConfirmPassword("newPassword");

        mockMvc.perform(
                put("/api/v1/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void changePassword_anonymous_returnsUnauthorized() throws Exception {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setToken("token");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        mockMvc.perform(
                put("/api/v1/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void updateAvatar_authenticated_returnsOk() throws Exception {
        doNothing().when(userInfoAppService).updateAvatar(any(Long.class), any());

        UpdateAvatarRequestDTO request = new UpdateAvatarRequestDTO();
        request.setFileId(100L);

        mockMvc.perform(
                put("/api/v1/user/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void updateAvatar_nonExistentFile_returnsNotFound() throws Exception {
        doThrow(new DataNotFound("文件不存在"))
                .when(userInfoAppService)
                .updateAvatar(any(Long.class), any());

        UpdateAvatarRequestDTO request = new UpdateAvatarRequestDTO();
        request.setFileId(9999L);

        mockMvc.perform(
                put("/api/v1/user/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updateAvatar_anonymous_returnsUnauthorized() throws Exception {
        UpdateAvatarRequestDTO request = new UpdateAvatarRequestDTO();
        request.setFileId(1L);

        mockMvc.perform(
                put("/api/v1/user/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
