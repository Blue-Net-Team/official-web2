package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.user.ChangeEmailRequestDTO;
import com.bluenet.web.api.dto.user.ChangePasswordRequestDTO;
import com.bluenet.web.api.dto.user.SendEmailVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.user.UpdateAvatarRequestDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.VerifyPasswordRequestDTO;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.infrastructure.security.change.ChangePasswordStateService;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.testsupport.fixture.FileFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserExperienceRepository userExperienceRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private ChangePasswordStateService changePasswordStateService;

    @MockBean
    private MessageDispatcher messageDispatcher;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private User saveMemberUser() {
        return UserFixture.member("2024001001").save(userRepository, passwordEncoder);
    }

    @Test
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void getMyInfo_authenticated_returnsUserInfo() throws Exception {
        User user = saveMemberUser();

        mockMvc.perform(get("/api/v1/user/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }

    @Test
    void getMyInfo_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void updateProfile_authenticated_returnsOk() throws Exception {
        saveMemberUser();

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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void getTabCounts_authenticated_returnsCounts() throws Exception {
        User user = saveMemberUser();

        UserExperience project = UserExperience.create(
                user.getId(),
                ExperienceType.PROJECT,
                "项目经历",
                "{}",
                null,
                null);
        UserExperience competition = UserExperience.create(
                user.getId(),
                ExperienceType.COMPETITION,
                "竞赛经历",
                "{}",
                null,
                null);
        userExperienceRepository.save(project);
        userExperienceRepository.save(competition);

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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void sendEmailVerificationCode_authenticated_returnsOk() throws Exception {
        User user = saveMemberUser();

        SendEmailVerificationCodeRequestDTO request = new SendEmailVerificationCodeRequestDTO();
        request.setEmail(user.getEmail());
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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void changeEmail_withValidCodes_returnsOk() throws Exception {
        User user = saveMemberUser();

        VerifyCode originalCode = VerifyCode.create(
                user.getEmail(),
                "123456",
                LocalDateTime.now().plusMinutes(5),
                "change-email-original");
        VerifyCode newCode = VerifyCode.create(
                "new@example.com",
                "654321",
                LocalDateTime.now().plusMinutes(5),
                "change-email-new");
        verificationCodeRepository.save(originalCode);
        verificationCodeRepository.save(newCode);

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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void verifyCurrentPassword_correct_returnsToken() throws Exception {
        saveMemberUser();

        VerifyPasswordRequestDTO request = new VerifyPasswordRequestDTO();
        request.setCurrentPassword("password");

        mockMvc.perform(
                post("/api/v1/user/password/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isString());
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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void changePassword_withValidToken_returnsOk() throws Exception {
        User user = saveMemberUser();
        String token = changePasswordStateService.create(user.getId());

        ChangePasswordRequestDTO changeRequest = new ChangePasswordRequestDTO();
        changeRequest.setToken(token);
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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void updateAvatar_authenticated_returnsOk() throws Exception {
        User user = saveMemberUser();
        File avatar = FileFixture.avatar("avatar.png");
        avatar.setUrl("http://example.com/avatar.png");
        File savedAvatar = fileRepository.save(avatar);

        UpdateAvatarRequestDTO request = new UpdateAvatarRequestDTO();
        request.setFileId(savedAvatar.getId());

        mockMvc.perform(
                put("/api/v1/user/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void updateAvatar_nonExistentFile_returnsNotFound() throws Exception {
        saveMemberUser();

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
